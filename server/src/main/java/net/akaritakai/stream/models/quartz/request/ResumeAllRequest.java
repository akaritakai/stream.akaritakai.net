package net.akaritakai.stream.models.quartz.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = ResumeAllRequest.ResumeAllRequestBuilder.class)
public class ResumeAllRequest {
    String key;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ResumeTriggerRequestBuilder {
    }
}
