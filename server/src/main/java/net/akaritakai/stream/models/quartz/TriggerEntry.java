package net.akaritakai.stream.models.quartz;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.json.InstantToNumberConverter;
import net.akaritakai.stream.json.NumberToInstantConverter;

import java.time.Instant;
import java.util.Map;

@Value
@Builder
@JsonDeserialize(builder = TriggerEntry.TriggerEntryBuilder.class)
public class TriggerEntry {
    @JsonInclude(JsonInclude.Include.NON_NULL) KeyEntry key;

    @JsonInclude(JsonInclude.Include.NON_NULL) KeyEntry job;

    Map<String, String> jobDataMap;

    String description;

    String calendar;

    int priority;

    boolean mayFireAgain;

    @JsonSerialize(converter = InstantToNumberConverter.class)
    @JsonDeserialize(converter = NumberToInstantConverter.class)
    Instant startTime;

    @JsonSerialize(converter = InstantToNumberConverter.class)
    @JsonDeserialize(converter = NumberToInstantConverter.class)
    Instant endTime;

    @JsonSerialize(converter = InstantToNumberConverter.class)
    @JsonDeserialize(converter = NumberToInstantConverter.class)
    Instant nextFireTime;

    @JsonSerialize(converter = InstantToNumberConverter.class)
    @JsonDeserialize(converter = NumberToInstantConverter.class)
    Instant previousFireTime;

    @JsonSerialize(converter = InstantToNumberConverter.class)
    @JsonDeserialize(converter = NumberToInstantConverter.class)
    Instant finalFireTime;

    int misfireInstruction;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TriggerEntryBuilder {
    }
}
