package net.akaritakai.stream.scheduling.jobs;

import net.akaritakai.stream.json.NumberToDurationConverter;
import net.akaritakai.stream.json.NumberToInstantConverter;
import net.akaritakai.stream.models.stream.StreamState;
import net.akaritakai.stream.models.stream.request.StreamStartRequest;
import net.akaritakai.stream.models.stream.request.StreamStopRequest;
import net.akaritakai.stream.scheduling.Utils;
import net.akaritakai.stream.streamer.Streamer;
import net.akaritakai.stream.streamer.StreamerListener;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class StreamPlayJob implements InterruptableJob, StreamerListener {

    private StreamStartRequest request;
    private CountDownLatch countDownLatch;

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        countDownLatch.countDown();
    }

    private StreamStartRequest buildRequest(JobExecutionContext context) {
        StreamStartRequest.StreamStartRequestBuilder builder = StreamStartRequest.builder()
                .name(context.get("streamName").toString());
        Optional.ofNullable(context.get("seekTime")).map(String::valueOf).map(Long::parseLong)
                .map(NumberToDurationConverter.INSTANCE::convert)
                .ifPresent(builder::seekTime);
        Optional.ofNullable(context.get("delay")).map(String::valueOf).map(Long::parseLong)
                .map(NumberToDurationConverter.INSTANCE::convert)
                .ifPresent(builder::delay);;
        Optional.ofNullable(context.get("startAt")).map(String::valueOf).map(Long::parseLong)
                .map(NumberToInstantConverter.INSTANCE::convert)
                .ifPresent(builder::startAt);
        return builder.build();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Streamer streamer = Objects.requireNonNull(Utils.get(context.getScheduler(), Streamer.KEY));
        try {
            request = buildRequest(context);
            countDownLatch = new CountDownLatch(1);

            streamer.startStream(request)
                    .toCompletionStage().toCompletableFuture().join();
            streamer.addListener(this);

            countDownLatch.await();

            StreamState state = streamer.getState();

            if (request.getName().equals(state.getMediaName())) {
              streamer.stopStream(StreamStopRequest.builder().build())
                      .toCompletionStage().toCompletableFuture().join();
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        } finally {
            streamer.removeListener(this);
        }
    }

    @Override
    public void onStateUpdate(StreamState state) {
        StreamStartRequest request = this.request;
        if (request == null) {
            return;
        }

        if (request.getName().equals(state.getMediaName())) {
            return;
        }

        countDownLatch.countDown();
    }
}
