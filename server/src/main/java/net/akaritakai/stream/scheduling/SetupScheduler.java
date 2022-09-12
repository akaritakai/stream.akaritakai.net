package net.akaritakai.stream.scheduling;

import net.akaritakai.stream.scheduling.jobs.*;
import org.quartz.JobBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public final class SetupScheduler {
    private SetupScheduler() {
    }

    public static void setup(Scheduler scheduler) throws SchedulerException {
        if (!scheduler.checkExists(JobKey.jobKey("ChatEnable"))) {
            scheduler.addJob(JobBuilder.newJob(ChatEnableJob.class).withIdentity("ChatEnable").storeDurably().build(), false);
        }

        if (!scheduler.checkExists(JobKey.jobKey("ChatDisable"))) {
            scheduler.addJob(JobBuilder.newJob(ChatDisableJob.class).withIdentity("ChatDisable").storeDurably().build(), false);
        }

        if (!scheduler.checkExists(JobKey.jobKey("ChatSend"))) {
            scheduler.addJob(JobBuilder.newJob(ChatSendJob.class).withIdentity("ChatSend").storeDurably().build(), false);
        }

        if (!scheduler.checkExists(JobKey.jobKey("StreamPlay"))) {
            scheduler.addJob(JobBuilder.newJob(StreamPlayJob.class).withIdentity("StreamPlay").storeDurably().build(), false);
        }

        if (!scheduler.checkExists(JobKey.jobKey("StreamStop"))) {
            scheduler.addJob(JobBuilder.newJob(StreamStopJob.class).withIdentity("StreamStop").storeDurably().build(), false);
        }
    }
}
