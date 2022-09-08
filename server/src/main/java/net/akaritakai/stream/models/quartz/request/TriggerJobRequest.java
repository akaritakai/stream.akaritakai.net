package net.akaritakai.stream.models.quartz.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.models.quartz.KeyEntry;

import java.util.Map;

@Value
@Builder
@JsonDeserialize(builder = TriggerJobRequest.TriggerJobRequestBuilder.class)
public class TriggerJobRequest {
    String key;

    KeyEntry jobKey;
    Map<String, String> jobDataMap;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TriggerJobRequestBuilder {
    }
}
