package net.akaritakai.stream.scheduling.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class AbstractJob implements Job {
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected String writeValueAsString(Object object) throws JobExecutionException {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JobExecutionException(e);
        }
    }

    protected <T> T readValue(String source, Class<T> clazz) throws JobExecutionException {
        try {
            return OBJECT_MAPPER.readValue(source, clazz);
        } catch (JsonProcessingException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public final void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            execute0(context);
        } catch (JobExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    protected abstract void execute0(JobExecutionContext context) throws Exception;
}