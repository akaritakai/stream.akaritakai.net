package net.akaritakai.stream.models.chat.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.akaritakai.stream.models.chat.ChatMessageType;


@Value
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(as = ChatSendRequest.class, builder = ChatSendRequest.ChatSendRequestBuilder.class)
public class ChatSendRequest extends ChatRequest {
  ChatMessageType messageType;
  String nickname;
  String message;

  @JsonPOJOBuilder(withPrefix = "")
  public static class ChatSendRequestBuilder {
  }
}
