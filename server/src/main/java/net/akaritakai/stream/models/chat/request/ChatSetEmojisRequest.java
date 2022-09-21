package net.akaritakai.stream.models.chat.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.models.chat.ChatEmojiEntry;

@Value
@Builder
@JsonDeserialize(builder = ChatSetEmojisRequest.ChatSetEmojisRequestBuilder.class)
public class ChatSetEmojisRequest {
    String key;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    ChatEmojiEntry emoji;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ChatSetEmojisRequestBuilder {
    }
}
