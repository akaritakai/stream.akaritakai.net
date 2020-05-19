package net.akaritakai.stream.models.chat;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.json.InstantToNumberConverter;
import net.akaritakai.stream.json.NumberToInstantConverter;


@Value
@Builder
@JsonDeserialize(builder = ChatSequence.ChatSequenceBuilder.class)
public class ChatSequence {
  @JsonSerialize(converter = InstantToNumberConverter.class) Instant epoch;
  long position;

  @JsonPOJOBuilder(withPrefix = "")
  public static class ChatSequenceBuilder implements ChatSequenceBuilderMixin {
  }

  private interface ChatSequenceBuilderMixin {
    @JsonDeserialize(converter = NumberToInstantConverter.class)
    ChatSequenceBuilder epoch(Instant epoch);
  }
}
