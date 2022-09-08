package net.akaritakai.stream.models.quartz.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.models.quartz.KeyEntry;

import java.util.Set;

@Value
@Builder
@JsonDeserialize(builder = PauseAllRequest.PauseAllRequestBuilder.class)
public class PauseAllRequest {
    String key;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PauseTriggerRequestBuilder {
    }
}
