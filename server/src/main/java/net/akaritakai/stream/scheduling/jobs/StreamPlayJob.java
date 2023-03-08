package net.akaritakai.stream.scheduling.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akaritakai.stream.json.NumberToDurationConverter;
import net.akaritakai.stream.json.NumberToInstantConverter;
import net.akaritakai.stream.models.stream.StreamState;
import net.akaritakai.stream.models.stream.request.StreamStartRequest;
import net.akaritakai.stream.models.stream.request.StreamStopRequest;
import net.akaritakai.stream.scheduling.Utils;
import net.akaritakai.stream.streamer.StreamerMBean;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import javax.management.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class StreamPlayJob implements InterruptableJob, NotificationListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
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
        ObjectName objectName = Utils.get(context.getScheduler(), StreamerMBean.KEY);
        StreamerMBean streamer = Utils.beanProxy(objectName, StreamerMBean.class);
        Handback handback = new Handback(buildRequest(context));
        try {
            countDownLatch = handback.latch;

            streamer.addNotificationListener(this, null, handback);
            try {
                streamer.startStream(objectMapper.writeValueAsString(handback.request));
                countDownLatch.await();

                StreamState state = objectMapper.readValue(streamer.getState(), StreamState.class);

                if (handback.request.getName().equals(state.getMediaName())) {
                    streamer.stopStream(objectMapper.writeValueAsString(StreamStopRequest.builder().build()));
                }
            } finally {
                try {
                    streamer.removeNotificationListener(this, null, handback);
                } catch (ListenerNotFoundException e) {
                    throw new JobExecutionException(e);
                }
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void handleNotification(Notification notification, Object handbackObject) {
        switch (notification.getMessage()) {
            case "StreamState":
                if (!(handbackObject instanceof Handback)) {
                    return;
                }
                Handback handback = (Handback) handbackObject;
                StreamState state = (StreamState) ((AttributeChangeNotification) notification).getNewValue();
                if (handback.request.getName().equals(state.getMediaName())) {
                    return;
                }

                handback.latch.countDown();
                break;
        }
    }

    private static class Handback {
        final StreamStartRequest request;
        final CountDownLatch latch;

        private Handback(StreamStartRequest request) {
            this.request = Objects.requireNonNull(request);
            this.latch = new CountDownLatch(1);
        }
    }
}
