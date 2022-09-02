package net.akaritakai.stream.scheduling;

import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static void triggerIfExists(Scheduler scheduler, String name) {
        triggerIfExists(scheduler, JobKey.jobKey(name));
    }
    public static void triggerIfExists(Scheduler scheduler, String name, String group) {
        triggerIfExists(scheduler, JobKey.jobKey(name, group));
    }
    public static void triggerIfExists(Scheduler scheduler, JobKey jobKey) {
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.triggerJob(jobKey);
            }
        } catch (SchedulerException e) {
            LOG.warn("Failed to trigger", e);
        }
    }

    public static void triggerIfExists(Scheduler scheduler, String name, JobDataMap jobDataMap) {
        triggerIfExists(scheduler, JobKey.jobKey(name), jobDataMap);
    }
    public static void triggerIfExists(Scheduler scheduler, String name, String group, JobDataMap jobDataMap) {
        triggerIfExists(scheduler, JobKey.jobKey(name, group), jobDataMap);
    }

    public static void triggerIfExists(Scheduler scheduler, JobKey jobKey, JobDataMap jobDataMap) {
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.triggerJob(jobKey, jobDataMap);
            }
        } catch (SchedulerException e) {
            LOG.warn("Failed to trigger", e);
        }
    }
}
