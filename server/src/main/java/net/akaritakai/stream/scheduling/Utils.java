package net.akaritakai.stream.scheduling;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    private static final WeakHashMap<Scheduler, ConcurrentMap<SchedulerAttribute<?>, AtomicReference<?>>> TRANSIENT
             = new WeakHashMap<>();

    private static <T> AtomicReference<T> getReference(Scheduler scheduler, SchedulerAttribute<T> key) {
        ConcurrentMap<SchedulerAttribute<T>, AtomicReference<T>> concurrentMap;
        synchronized (TRANSIENT) {
            @SuppressWarnings({"UnnecessaryLocalVariable", "rawtypes"}) ConcurrentMap map
                    = TRANSIENT.computeIfAbsent(scheduler, ignore -> new ConcurrentHashMap<>());
            //noinspection unchecked
            concurrentMap = map;
        }
        return concurrentMap.computeIfAbsent(key, ignore -> new AtomicReference<>());
    }

    public static <T> T get(Scheduler scheduler, SchedulerAttribute<T> key) {
        return getReference(scheduler, key).get();
    }

    public static <T> T set(Scheduler scheduler, SchedulerAttribute<T> key, T value) {
        return getReference(scheduler, key).getAndSet(value);
    }

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
