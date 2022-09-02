package net.akaritakai.stream.models.info;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = LogFetchRequest.LogFetchRequestBuilder.class)
public class LogFetchRequest {
    String key;

    @JsonPOJOBuilder(withPrefix = "")
    public static class LogFetchRequestBuilder {
    }
}
