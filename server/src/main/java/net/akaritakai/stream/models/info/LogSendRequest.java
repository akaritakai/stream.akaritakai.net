package net.akaritakai.stream.models.info;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = LogSendRequest.LogSendRequestBuilder.class)
public class LogSendRequest {
    String key;

    @JsonInclude(JsonInclude.Include.NON_NULL) String log;

    @JsonPOJOBuilder(withPrefix = "")
    public static class LogSendRequestBuilder {
    }
}
