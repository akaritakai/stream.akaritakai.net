package net.akaritakai.stream.streamer;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import net.akaritakai.stream.client.AwsS3Client;
import net.akaritakai.stream.client.FakeAwsS3Client;
import net.akaritakai.stream.config.ConfigData;
import net.akaritakai.stream.exception.StreamStateConflictException;
import net.akaritakai.stream.models.stream.StreamState;
import net.akaritakai.stream.models.stream.StreamStateType;
import net.akaritakai.stream.models.stream.request.StreamPauseRequest;
import net.akaritakai.stream.models.stream.request.StreamResumeRequest;
import net.akaritakai.stream.models.stream.request.StreamStartRequest;
import net.akaritakai.stream.models.stream.request.StreamStopRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Streamer {
  private static final Logger LOG = LoggerFactory.getLogger(Streamer.class);

  private final Vertx _vertx;
  private final AwsS3Client _client;
  private final String _livePlaylistUrl;
  private final AtomicReference<StreamState> _state = new AtomicReference<>(StreamState.OFFLINE);
  private final Set<StreamerListener> _listeners = ConcurrentHashMap.newKeySet();

  public Streamer(Vertx vertx, ConfigData config) {
    _vertx = vertx;
    if (config.isDevelopment()) {
      _client = new FakeAwsS3Client(vertx, config);
    } else {
      _client = new AwsS3Client(vertx, config);
    }
    _livePlaylistUrl = config.getLivePlaylistUrl();
  }

  public Future<Void> startStream(StreamStartRequest request) {
    LOG.info("Got request to start the stream: {}", request);
    Promise<Void> promise = Promise.promise();
    _vertx.runOnContext(startStreamEvent -> {
      // Ensure that the stream is not already running
      StreamState state = _state.get();
      if (state == null || state.getStatus() != StreamStateType.OFFLINE) {
        LOG.warn("Can't start the stream: the stream is already running");
        promise.fail(new StreamStateConflictException("The stream is already running"));
        return;
      }

      if (request.isLive()) {
        // Stream is live

        // Use the request provided delay or none at all
        Duration delay = Optional.ofNullable(request.getDelay()).orElse(Duration.ZERO);
        // We use the provided start time + delay, or now + delay
        Instant startTime = Optional.ofNullable(request.getStartAt()).orElse(Instant.now()).plus(delay);

        // Create the new state
        StreamState newState = StreamState.builder()
                .status(StreamStateType.ONLINE)
                .playlist(_livePlaylistUrl)
                .mediaName("LIVE")
                .startTime(startTime)
                .live(true)
                .build();
        setState(newState);

        // Notify all the listeners that the stream changed
        _listeners.forEach(listener -> _vertx.runOnContext(e -> listener.onStateUpdate(_state.get())));

        // Stream start requested successfully
        promise.complete();
      } else {
        // Stream is pre-recorded

        // Load the stream's metadata
        _client.getMetadata(request.getName()).onFailure(promise::fail).onSuccess(metadata -> {
          // Use the request provided delay or none at all
          Duration delay = Optional.ofNullable(request.getDelay()).orElse(Duration.ZERO);
          // Use the request provided seek time or the default one
          Duration seekTime = Optional.ofNullable(request.getSeekTime()).orElse(Duration.ZERO);
          // We use the provided start time + delay, or now + delay
          Instant startTime = Optional.ofNullable(request.getStartAt()).orElse(Instant.now()).plus(delay);
          // We end at startTime + media duration - seek time
          Instant endTime = startTime.plus(metadata.getDuration()).minus(seekTime);

          // Create the new state
          StreamState newState = StreamState.builder()
                  .status(StreamStateType.ONLINE)
                  .playlist(metadata.getPlaylist())
                  .mediaName(metadata.getName())
                  .mediaDuration(metadata.getDuration())
                  .startTime(startTime)
                  .endTime(endTime)
                  .seekTime(seekTime)
                  .live(false)
                  .build();
          setState(newState);

          // Take the stream offline at the end of the media
          _vertx.setTimer(Duration.between(Instant.now(), endTime).toMillis(), eventEnd -> {
            // Only take the stream online if the currently running stream is the stream we're supposed to end
            if (Objects.equals(_state.get(), newState)) {
              setState(StreamState.OFFLINE);
              _listeners.forEach(listener -> _vertx.runOnContext(e -> listener.onStateUpdate(_state.get())));
            }
          });

          // Notify all the listeners that the stream changed
          _listeners.forEach(listener -> _vertx.runOnContext(e -> listener.onStateUpdate(_state.get())));

          // Stream start requested successfully
          promise.complete();
        });
      }
    });
    return promise.future();
  }

  public Future<Void> pauseStream(StreamPauseRequest request) {
    LOG.info("Got request to pause the stream: {}", request);
    Promise<Void> promise = Promise.promise();
    _vertx.runOnContext(pauseStreamEvent -> {
      // Ensure that the stream is already running
      StreamState state = _state.get();
      if (state == null || state.getStatus() != StreamStateType.ONLINE) {
        LOG.warn("Can't stop the stream: the stream is not running");
        promise.fail(new StreamStateConflictException("The stream is not currently running"));
        return;
      }

      if (state.isLive()) {
        // Stream is live

        // Create the new state
        StreamState newState = StreamState.builder()
                .status(StreamStateType.PAUSE)
                .playlist(state.getPlaylist())
                .mediaName(state.getMediaName())
                .live(true)
                .build();
        setState(newState);
      } else {
        // Stream is pre-recorded

        // Determine the virtual start time (start time - seek time)
        Instant startTime = state.getStartTime().minus(state.getSeekTime());
        // Determine the seek time we should pause at (now - virtual start time)
        Duration seekTime = Duration.between(startTime, Instant.now());

        // Create the new state
        StreamState newState = StreamState.builder()
                .status(StreamStateType.PAUSE)
                .playlist(state.getPlaylist())
                .mediaName(state.getMediaName())
                .mediaDuration(state.getMediaDuration())
                .live(false)
                .seekTime(seekTime)
                .build();
        setState(newState);
      }

      // Notify all the listeners that the stream changed
      _listeners.forEach(listener -> _vertx.runOnContext(e -> listener.onStateUpdate(_state.get())));

      // Stream pause requested successfully
      promise.complete();
    });
    return promise.future();
  }

  public Future<Void> resumeStream(StreamResumeRequest request) {
    LOG.info("Got request to resume the stream: {}", request);
    Promise<Void> promise = Promise.promise();
    _vertx.runOnContext(resumeStreamEvent -> {
      // Ensure that the stream is paused
      StreamState state = _state.get();
      if (state == null || state.getStatus() != StreamStateType.PAUSE) {
        LOG.warn("Can't stop the stream: the stream is not paused");
        promise.fail(new StreamStateConflictException("The stream is not currently paused"));
        return;
      }

      if (state.isLive()) {
        // Stream is live
        // Use the request provided delay or none at all
        Duration delay = Optional.ofNullable(request.getDelay()).orElse(Duration.ZERO);
        // We start at the provided start time + delay, or now + delay
        Instant startTime = Optional.ofNullable(request.getStartAt()).orElse(Instant.now()).plus(delay);

        // Create the new state
        StreamState newState = StreamState.builder()
                .status(StreamStateType.ONLINE)
                .playlist(_livePlaylistUrl)
                .mediaName("LIVE")
                .startTime(startTime)
                .live(true)
                .build();
        setState(newState);
      } else {
        // Stream is pre-recorded

        // Use the request provided delay or none at all
        Duration delay = Optional.ofNullable(request.getDelay()).orElse(Duration.ZERO);
        // Determine our new seek time (use request seek time or the time we paused at)
        Duration seekTime = Optional.ofNullable(request.getSeekTime()).orElse(state.getSeekTime());
        // We start at the provided start time + delay, or now + delay
        Instant startTime = Optional.ofNullable(request.getStartAt()).orElse(Instant.now()).plus(delay);
        // We end at startTime + media duration - seek time
        Instant endTime = startTime.plus(state.getMediaDuration()).minus(seekTime);

        // Create the new state
        StreamState newState = StreamState.builder()
                .status(StreamStateType.ONLINE)
                .playlist(state.getPlaylist())
                .mediaName(state.getMediaName())
                .mediaDuration(state.getMediaDuration())
                .startTime(startTime)
                .endTime(endTime)
                .seekTime(seekTime)
                .live(false)
                .build();
        setState(newState);

        // Take the stream offline at the end of the media
        _vertx.setTimer(Duration.between(Instant.now(), endTime).toMillis(), eventEnd -> {
          // Only take the stream online if the currently running stream is the stream we're supposed to end
          if (Objects.equals(_state.get(), newState)) {
            setState(StreamState.OFFLINE);
            _listeners.forEach(listener -> _vertx.runOnContext(e -> listener.onStateUpdate(_state.get())));
          }
        });
      }

      // Notify all the listeners that the stream changed
      _listeners.forEach(listener -> _vertx.runOnContext(e -> listener.onStateUpdate(_state.get())));

      // Stream resume requested successfully
      promise.complete();
    });
    return promise.future();
  }

  public Future<Void> stopStream(StreamStopRequest request) {
    LOG.info("Got request to stop the stream: {}", request);
    Promise<Void> promise = Promise.promise();
    _vertx.runOnContext(stopStreamEvent -> {
      // Ensure that the stream is not already stopped
      StreamState state = _state.get();
      if (state == null || state.getStatus() == StreamStateType.OFFLINE) {
        LOG.warn("Can't stop the stream: the stream is already stopped");
        promise.fail(new StreamStateConflictException("The stream is already stopped"));
        return;
      }

      // Set the new state
      setState(StreamState.OFFLINE);
      // Notify all the listeners that the stream changed
      _listeners.forEach(listener -> _vertx.runOnContext(e -> listener.onStateUpdate(_state.get())));

      // Stream stop requested successfully
      promise.complete();
    });
    return promise.future();
  }

  public StreamState getState() {
    return _state.get();
  }

  public void setState(StreamState state) {
    _state.set(state);
    LOG.info("New state = {}", state);
  }

  public void addListener(StreamerListener listener) {
    _listeners.add(listener);
  }
}
