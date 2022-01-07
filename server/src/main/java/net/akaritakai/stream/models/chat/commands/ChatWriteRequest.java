package net.akaritakai.stream.models.chat.commands;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.models.chat.ChatMessageType;

@Value
@Builder
@JsonDeserialize(builder = ChatWriteRequest.ChatWriteRequestBuilder.class)
public class ChatWriteRequest {
  String key;
  ChatMessageType messageType;
  String nickname;
  String message;

  @JsonPOJOBuilder(withPrefix = "")
  public static class ChatWriteRequestBuilder {
  }
}
