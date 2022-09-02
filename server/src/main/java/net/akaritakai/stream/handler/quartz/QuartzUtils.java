package net.akaritakai.stream.handler.quartz;

import org.quartz.JobDataMap;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class QuartzUtils {
    public static Map<String, String> mapOf(JobDataMap jobDataMap) {
        if (jobDataMap == null) {
            return null;
        }
        if (jobDataMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>(jobDataMap.size());
        jobDataMap.forEach((key, value) -> map.put(key, value != null ? value.toString() : null));
        return map;
    }

    public static Instant instantOf(Date date) {
        return date != null ? date.toInstant() : null;
    }
}
