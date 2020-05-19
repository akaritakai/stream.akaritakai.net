package net.akaritakai.stream.models.chat;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
@JsonDeserialize(builder = ChatMessage.ChatMessageBuilder.class)
public class ChatMessage {
  ChatSequence sequence;
  ChatMessageType messageType;
  String nickname;
  String message;

  @JsonPOJOBuilder(withPrefix = "")
  public static class ChatMessageBuilder {
  }
}
