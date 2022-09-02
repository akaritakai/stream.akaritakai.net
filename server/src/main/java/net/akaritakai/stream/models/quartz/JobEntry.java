package net.akaritakai.stream.models.quartz;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
@JsonDeserialize(builder = JobEntry.JobEntryBuilder.class)
public class JobEntry {
    @JsonInclude(JsonInclude.Include.NON_NULL) KeyEntry key;

    @JsonInclude(JsonInclude.Include.NON_NULL) String clazz;

    Map<String, String> jobDataMap;

    String description;

    boolean durable;

    boolean persist;

    boolean concurrent;

    boolean recoverable;

    @JsonPOJOBuilder(withPrefix = "")
    public static class JobEntryBuilder {
    }
}
