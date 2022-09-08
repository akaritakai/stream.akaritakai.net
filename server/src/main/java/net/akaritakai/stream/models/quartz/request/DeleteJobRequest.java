package net.akaritakai.stream.models.quartz.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.models.quartz.KeyEntry;

import java.util.Set;

@Value
@Builder
@JsonDeserialize(builder = DeleteJobRequest.DeleteJobRequestBuilder.class)
public class DeleteJobRequest {
    String key;

    Set<KeyEntry> jobKeys;

    @JsonPOJOBuilder(withPrefix = "")
    public static class DeleteJobRequestBuilder {
    }
}
