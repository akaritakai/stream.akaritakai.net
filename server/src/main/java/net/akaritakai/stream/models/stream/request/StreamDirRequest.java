package net.akaritakai.stream.models.stream.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = StreamDirRequest.StreamDirRequestBuilder.class)
public class StreamDirRequest {
    String key;
    String filter;

    @JsonPOJOBuilder(withPrefix = "")
    public static class StreamDirRequestBuilder {
    }
}
