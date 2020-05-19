package net.akaritakai.stream.models.chat.commands;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
@JsonDeserialize(builder = ChatClearRequest.ChatClearRequestBuilder.class)
public class ChatClearRequest {
  String key;

  @JsonPOJOBuilder(withPrefix = "")
  public static class ChatClearRequestBuilder {
  }
}
