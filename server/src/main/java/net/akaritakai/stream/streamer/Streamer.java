package net.akaritakai.stream.streamer;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import net.akaritakai.stream.client.AwsS3Client;
import net.akaritakai.stream.client.FakeAwsS3Client;
import net.akaritakai.stream.config.ConfigData;
import net.akaritakai.stream.exception.StreamStateConflictException;
import net.akaritakai.stream.models.stream.StreamEntry;
import net.akaritakai.stream.models.stream.StreamState;
import net.akaritakai.stream.models.stream.StreamStateType;
import net.akaritakai.stream.models.stream.request.StreamPauseRequest;
import net.akaritakai.stream.models.stream.request.StreamResumeRequest;
import net.akaritakai.stream.models.stream.request.StreamStartRequest;
import net.akaritakai.stream.models.stream.request.StreamStopRequest;
import net.akaritakai.stream.scheduling.SchedulerAttribute;
import net.akaritakai.stream.scheduling.Utils;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Streamer {
  public static final SchedulerAttribute<Streamer> KEY = SchedulerAttribute.instanceOf("streamer", Streamer.class);
    private static final Logger LOG = LoggerFactory.getLogger(Streamer.class);

  private final Vertx _vertx;
  private final AwsS3Client _client;
  private final String _livePlaylistUrl;
  private final AtomicReference<StreamState> _state = new AtomicReference<>(StreamState.OFFLINE);
  private final Set<StreamerListener> _listeners = ConcurrentHashMap.newKeySet();

  private final Scheduler _scheduler;

  public Streamer(Vertx vertx, ConfigData config, Scheduler scheduler) {
    _vertx = vertx;
    _scheduler = scheduler;
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
    _vertx.runOnContext(aVoid -> startStream0(request, promise));
    return promise.future();
  }

  private void startStream0(StreamStartRequest request, Promise<Void> promise) {
    try {
      // Ensure that the stream is not already running
      StreamState state = _state.get();
      if (state == null || state.getStatus() != StreamStateType.OFFLINE) {
        LOG.warn("Can't start the stream: the stream is already running");
        promise.fail(new StreamStateConflictException("The stream is already running"));
        return;
      }

      // Use the request provided delay or none at all
      Duration delay = Optional.ofNullable(request.getDelay()).orElse(Duration.ZERO);
      LOG.info("delay={}", delay);
      // We use the provided start time + delay, or now + delay
      Instant startTime = Optional.ofNullable(request.getStartAt()).orElse(Instant.now()).plus(delay);

      _client.getMetadata(request.getName()).onFailure(promise::fail).onSuccess(metadata -> {
        try {
          // Create the new state
          StreamState.StreamStateBuilder builder = StreamState.builder()
                  .status(StreamStateType.ONLINE)
                  .playlist(metadata.getPlaylist())
                  .mediaName(Optional.ofNullable(metadata.getName()).orElse("LIVE"))
                  .startTime(startTime)
                  .live(metadata.isLive());

          Instant endTime;
          if (!metadata.isLive()) {
            // Use the request provided seek time or the default one
            Duration seekTime = Optional.ofNullable(request.getSeekTime()).orElse(Duration.ZERO);
            // We end at startTime + media duration - seek time
            endTime = startTime.plus(metadata.getDuration()).minus(seekTime);

            builder.mediaDuration(metadata.getDuration())
                    .seekTime(seekTime)
                    .endTime(endTime);
          } else {
            endTime = null;
          }

          StreamState newState = builder.build();

          setState(newState);

          // Stream start requested successfully
          promise.complete();

        } catch (Throwable ex) {
          promise.fail(ex);
        }
      });
    } catch (Throwable ex) {
      promise.fail(ex);
    }
  }

  public Future<Void> pauseStream(StreamPauseRequest request) {
    LOG.info("Got request to pause the stream: {}", request);
    Promise<Void> promise = Promise.promise();
    _vertx.runOnContext(aVoid -> pauseStream0(request, promise));
    return promise.future();
  }

  private void pauseStream0(StreamPauseRequest request, Promise<Void> promise) {
    try {
      // Ensure that the stream is already running
      StreamState state = _state.get();
      if (state == null || state.getStatus() != StreamStateType.ONLINE) {
        LOG.warn("Can't stop the stream: the stream is not running");
        promise.fail(new StreamStateConflictException("The stream is not currently running"));
        return;
      }

      StreamState newState;
      if (state.isLive()) {
        // Stream is live

        // Create the new state
        newState = StreamState.builder()
                .status(StreamStateType.PAUSE)
                .playlist(state.getPlaylist())
                .mediaName(state.getMediaName())
                .live(true)
                .build();
      } else {
        // Stream is pre-recorded

        // Determine the virtual start time (start time - seek time)
        Instant startTime = state.getStartTime().minus(state.getSeekTime());
        // Determine the seek time we should pause at (now - virtual start time)
        Duration seekTime = Duration.between(startTime, Instant.now());

        // Create the new state
        newState = StreamState.builder()
                .status(StreamStateType.PAUSE)
                .playlist(state.getPlaylist())
                .mediaName(state.getMediaName())
                .mediaDuration(state.getMediaDuration())
                .live(false)
                .seekTime(seekTime)
                .build();
      }

      setState(newState);

      // Stream pause requested successfully
      promise.complete();
    } catch (Throwable ex) {
      promise.fail(ex);
    }
  }

  public Future<Void> resumeStream(StreamResumeRequest request) {
    LOG.info("Got request to resume the stream: {}", request);
    Promise<Void> promise = Promise.promise();
    _vertx.runOnContext(aVoid -> resumeStream0(request, promise));
    return promise.future();
  }

  private void resumeStream0(StreamResumeRequest request, Promise<Void> promise) {
    try {
      // Ensure that the stream is paused
      StreamState state = _state.get();
      if (state == null || state.getStatus() != StreamStateType.PAUSE) {
        LOG.warn("Can't stop the stream: the stream is not paused");
        promise.fail(new StreamStateConflictException("The stream is not currently paused"));
        return;
      }

      // Use the request provided delay or none at all
      Duration delay = Optional.ofNullable(request.getDelay()).orElse(Duration.ZERO);
      // We start at the provided start time + delay, or now + delay
      Instant startTime = Optional.ofNullable(request.getStartAt()).orElse(Instant.now()).plus(delay);

      StreamState.StreamStateBuilder builder = StreamState.builder()
              .status(StreamStateType.ONLINE)
              .playlist(state.getPlaylist())
              .mediaName(state.getMediaName())
              .startTime(startTime)
              .live(state.isLive());

      Instant endTime;
      if (!state.isLive()) {
        // Determine our new seek time (use request seek time or the time we paused at)
        Duration seekTime = Optional.ofNullable(request.getSeekTime()).orElse(state.getSeekTime());
        // We end at startTime + media duration - seek time
        endTime = startTime.plus(state.getMediaDuration()).minus(seekTime);

        builder.mediaDuration(state.getMediaDuration())
                .endTime(endTime)
                .seekTime(seekTime);
      } else {
        endTime = null;
      }

      StreamState newState = builder.build();

      setState(newState);

      // Stream resume requested successfully
      promise.complete();
    } catch (Throwable ex) {
      promise.fail(ex);
    }
  }

  public Future<Void> stopStream(StreamStopRequest request) {
    LOG.info("Got request to stop the stream: {}", request);
    Promise<Void> promise = Promise.promise();
    _vertx.runOnContext(aVoid -> stopStream0(request, promise));
    return promise.future();
  }

  private void stopStream0(StreamStopRequest request, Promise<Void> promise) {
    try {
      // Ensure that the stream is not already stopped
      StreamState state = _state.get();
      if (state == null || state.getStatus() == StreamStateType.OFFLINE) {
        LOG.warn("Can't stop the stream: the stream is already stopped");
        promise.fail(new StreamStateConflictException("The stream is already stopped"));
        return;
      }

      // Set the new state
      setState(StreamState.OFFLINE);

      // Stream stop requested successfully
      promise.complete();
    } catch (Throwable ex) {
      promise.fail(ex);
    }
  }

  public StreamState getState() {
    return _state.get();
  }

  public void setState(StreamState state) {
    StreamState old = _state.getAndSet(Objects.requireNonNull(state));
    if (old != state && !state.equals(old)) {
      LOG.info("New state = {}", state);
      JobDataMap jobDataMap = new JobDataMap();
      jobDataMap.put("status", state.getStatus());
      jobDataMap.put("live", state.isLive());
      jobDataMap.put("playlist", state.getPlaylist());
      jobDataMap.put("mediaName", state.getMediaName());
      jobDataMap.put("mediaDuration", state.getMediaDuration());
      jobDataMap.put("startTime", state.getStartTime());
      jobDataMap.put("endTime", state.getEndTime());
      jobDataMap.put("seekTime", state.getSeekTime());
      Utils.triggerIfExists(_scheduler, "Stream", String.valueOf(state), jobDataMap);

      // Notify all the listeners that the stream changed
      _listeners.forEach(listener -> _vertx.runOnContext(e -> listener.onStateUpdate(_state.get())));

      if (state.getStatus() == StreamStateType.ONLINE && state.getEndTime() != null) {
        // Take the stream offline at the end of the media
        _vertx.setTimer(Math.max(Duration.between(Instant.now(), state.getEndTime()).toMillis(), 1), eventEnd -> {
          // Only take the stream online if the currently running stream is the stream we're supposed to end
          if (Objects.equals(_state.get(), state)) {
            setState(StreamState.OFFLINE);
          }
        });
      }
    }
  }

  public Future<List<StreamEntry>> listStreams(String filter) {
    try {
      return _client.listMetadataNames(filter != null && !filter.isBlank() ? new Predicate<String>() {
        final Pattern pattern = Pattern.compile(filter.trim(), Pattern.CASE_INSENSITIVE);

        @Override
        public boolean test(String s) {
          return pattern.matcher(s).find();
        }
      } : any -> true);
    } catch (java.util.regex.PatternSyntaxException ex) {
      return Future.succeededFuture(Collections.emptyList());
    }
  }

  public void addListener(StreamerListener listener) {
    _listeners.add(listener);
  }

  public void removeListener(StreamerListener listener) {
    _listeners.remove(listener);
  }
}
