package net.akaritakai.stream.models.chat.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;


@Value
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(as = ChatPartRequest.class, builder = ChatPartRequest.ChatPartRequestBuilder.class)
public class ChatPartRequest extends ChatRequest {
  @JsonPOJOBuilder(withPrefix = "")
  public static class ChatPartRequestBuilder {
  }
}
