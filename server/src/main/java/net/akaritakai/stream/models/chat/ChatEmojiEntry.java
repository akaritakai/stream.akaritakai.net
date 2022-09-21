package net.akaritakai.stream.models.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.json.StringToURLConverter;
import net.akaritakai.stream.json.URLToStringConverter;

import java.net.URL;

@Value
@Builder
@JsonDeserialize(builder = ChatEmojiEntry.ChatEmojiEntryBuilder.class)
public class ChatEmojiEntry {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String name;

    @JsonSerialize(converter = URLToStringConverter.class)
    URL url;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ChatEmojiEntryBuilder implements ChatEmojiEntryBuilderMixin {
    }

    private interface ChatEmojiEntryBuilderMixin {
        @JsonDeserialize(converter = StringToURLConverter.class)
        ChatEmojiEntryBuilder url(URL url);
    }

}
