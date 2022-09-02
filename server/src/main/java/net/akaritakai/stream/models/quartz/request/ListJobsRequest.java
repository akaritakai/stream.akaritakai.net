package net.akaritakai.stream.models.quartz.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = ListJobsRequest.ListJobsRequestBuilder.class)
public class ListJobsRequest {
    String key;

    String groupPrefix;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ListJobsRequestBuilder {
    }
}
