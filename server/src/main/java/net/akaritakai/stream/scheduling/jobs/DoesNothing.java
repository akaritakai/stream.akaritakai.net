package net.akaritakai.stream.scheduling.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DoesNothing implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        context.setResult(Thread.currentThread());
    }
}
