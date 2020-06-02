package net.akaritakai.stream.models.stream;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.json.DurationToNumberConverter;
import net.akaritakai.stream.json.NumberToDurationConverter;


@Value
@Builder
@JsonDeserialize(builder = StreamMetadata.StreamMetadataBuilder.class)
public class StreamMetadata {
  String name;
  String playlist;
  boolean live;
  @JsonInclude(Include.NON_NULL) @JsonSerialize(converter = DurationToNumberConverter.class) Duration duration;

  @JsonPOJOBuilder(withPrefix = "")
  public static class StreamMetadataBuilder implements StreamMetadataBuilderMixin {
  }

  private interface StreamMetadataBuilderMixin {
    @JsonDeserialize(converter = NumberToDurationConverter.class)
    StreamMetadataBuilder duration(Duration duration);
  }
}
