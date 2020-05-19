package net.akaritakai.stream.models.chat.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.akaritakai.stream.models.chat.ChatMessage;


@Value
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(as = ChatMessageResponse.class, builder = ChatMessageResponse.ChatMessageResponseBuilder.class)
public class ChatMessageResponse extends ChatResponse {
  ChatMessage message;

  @JsonPOJOBuilder(withPrefix = "")
  public static class ChatMessageResponseBuilder {
  }
}
