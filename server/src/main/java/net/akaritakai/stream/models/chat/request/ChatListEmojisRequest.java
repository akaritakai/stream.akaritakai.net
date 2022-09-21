package net.akaritakai.stream.models.chat.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = ChatListEmojisRequest.ChatListEmojisRequestBuilder.class)
public class ChatListEmojisRequest {
    String key;
    String filter;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ChatListEmojisRequestBuilder {
    }
}
