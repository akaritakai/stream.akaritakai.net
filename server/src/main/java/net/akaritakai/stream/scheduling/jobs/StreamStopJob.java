package net.akaritakai.stream.scheduling.jobs;

import net.akaritakai.stream.models.stream.request.StreamStopRequest;
import net.akaritakai.stream.scheduling.Utils;
import net.akaritakai.stream.streamer.Streamer;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Objects;

public class StreamStopJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        StreamStopRequest request = StreamStopRequest.builder()
                .build();
        Streamer streamer = Objects.requireNonNull(Utils.get(context.getScheduler(), Streamer.KEY));
        try {
            streamer.stopStream(request).toCompletionStage().toCompletableFuture().join();
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
