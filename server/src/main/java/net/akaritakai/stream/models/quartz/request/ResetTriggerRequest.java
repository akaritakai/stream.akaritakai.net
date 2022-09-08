package net.akaritakai.stream.models.quartz.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.models.quartz.KeyEntry;

@Value
@Builder
@JsonDeserialize(builder = ResetTriggerRequest.ResetTriggerRequestBuilder.class)
public class ResetTriggerRequest {
    String key;

    KeyEntry triggerKey;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ResetTriggerJobRequestBuilder {
    }
}
