package net.akaritakai.stream.models.chat.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.akaritakai.stream.models.chat.ChatMessage;
import net.akaritakai.stream.models.chat.ChatSequence;


@Value
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(as = ChatStatusResponse.class, builder = ChatStatusResponse.ChatStatusResponseBuilder.class)
public class ChatStatusResponse extends ChatResponse {
  boolean enabled;
  @JsonInclude(JsonInclude.Include.NON_NULL) ChatSequence sequence;
  @JsonInclude(JsonInclude.Include.NON_NULL) List<ChatMessage> messages;

  @JsonPOJOBuilder(withPrefix = "")
  public static class ChatStatusResponseBuilder {
  }
}
