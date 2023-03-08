package net.akaritakai.stream.scheduling.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akaritakai.stream.models.stream.request.StreamStopRequest;
import net.akaritakai.stream.scheduling.Utils;
import net.akaritakai.stream.streamer.StreamerMBean;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.management.ObjectName;

public class StreamStopJob implements Job {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        StreamStopRequest request = StreamStopRequest.builder()
                .build();
        ObjectName objectName = Utils.get(context.getScheduler(), StreamerMBean.KEY);
        StreamerMBean streamer = Utils.beanProxy(objectName, StreamerMBean.class);
        try {
            streamer.stopStream(objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
