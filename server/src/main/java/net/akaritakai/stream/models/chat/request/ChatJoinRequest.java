package net.akaritakai.stream.models.chat.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.akaritakai.stream.models.chat.ChatSequence;


@Value
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(as = ChatJoinRequest.class, builder = ChatJoinRequest.ChatJoinRequestBuilder.class)
public class ChatJoinRequest extends ChatRequest {
  @JsonInclude(JsonInclude.Include.NON_NULL) ChatSequence sequence;

  String nickname;

  @JsonPOJOBuilder(withPrefix = "")
  public static class ChatJoinRequestBuilder {
  }
}
