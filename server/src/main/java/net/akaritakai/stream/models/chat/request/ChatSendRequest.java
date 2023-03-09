package net.akaritakai.stream.models.chat.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.akaritakai.stream.json.InetAddressToStringConverter;
import net.akaritakai.stream.json.StringToInetAddressConverter;
import net.akaritakai.stream.models.chat.ChatMessageType;

import java.net.InetAddress;


@Value
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(as = ChatSendRequest.class, builder = ChatSendRequest.ChatSendRequestBuilder.class)
public class ChatSendRequest extends ChatRequest {
  ChatMessageType messageType;
  String nickname;
  String message;

  @JsonSerialize(converter = InetAddressToStringConverter.class)
  InetAddress source;

  @JsonPOJOBuilder(withPrefix = "")
  public static class ChatSendRequestBuilder implements ChatSendRequestBuilderMixin {
  }

  private interface ChatSendRequestBuilderMixin {
    @JsonDeserialize(converter = StringToInetAddressConverter.class)
    ChatSendRequestBuilderMixin source(InetAddress ipAddress);
  }

}
