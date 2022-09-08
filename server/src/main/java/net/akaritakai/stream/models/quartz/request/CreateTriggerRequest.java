package net.akaritakai.stream.models.quartz.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.json.InstantToNumberConverter;
import net.akaritakai.stream.json.NumberToInstantConverter;
import net.akaritakai.stream.models.quartz.KeyEntry;
import net.akaritakai.stream.models.quartz.ScheduleEntry;

import java.time.Instant;
import java.util.Map;

@Value
@Builder
@JsonDeserialize(builder = CreateTriggerRequest.CreateTriggerRequestBuilder.class)
public class CreateTriggerRequest {
    String key;

    KeyEntry triggerKey;
    String description;
    int priority;
    String calendar;

    @JsonSerialize(converter = InstantToNumberConverter.class)
    @JsonDeserialize(converter = NumberToInstantConverter.class)
    Instant startAt;

    @JsonSerialize(converter = InstantToNumberConverter.class)
    @JsonDeserialize(converter = NumberToInstantConverter.class)
    Instant endAt;

    ScheduleEntry schedule;

    KeyEntry jobKey;
    Map<String, String> jobDataMap;



    @JsonPOJOBuilder(withPrefix = "")
    public static class CreateTriggerRequestBuilder {
    }
}
