package net.akaritakai.stream.models.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.akaritakai.stream.models.chat.ChatEmojiEntry;

import java.util.List;

@Value
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = ChatListEmojisResponse.ChatListEmojisResponseBuilder.class)
public class ChatListEmojisResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<ChatEmojiEntry> entries;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ChatListEmojisResponseBuilder {
    }
}
