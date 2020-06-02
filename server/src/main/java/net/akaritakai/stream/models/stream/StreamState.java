package net.akaritakai.stream.models.stream;

import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.json.DurationToNumberConverter;
import net.akaritakai.stream.json.InstantToNumberConverter;
import net.akaritakai.stream.json.NumberToDurationConverter;
import net.akaritakai.stream.json.NumberToInstantConverter;


@Value
@Builder
@JsonDeserialize(builder = StreamState.StreamStateBuilder.class)
public class StreamState {
  public static final StreamState OFFLINE = StreamState.builder().status(StreamStateType.OFFLINE).build();

  StreamStateType status;
  boolean live;
  @JsonInclude(Include.NON_NULL) String playlist;
  @JsonInclude(Include.NON_NULL) String mediaName;
  @JsonInclude(Include.NON_NULL) @JsonSerialize(converter = DurationToNumberConverter.class) Duration mediaDuration;
  @JsonInclude(Include.NON_NULL) @JsonSerialize(converter = InstantToNumberConverter.class) Instant startTime;
  @JsonInclude(Include.NON_NULL) @JsonSerialize(converter = InstantToNumberConverter.class) Instant endTime;
  @JsonInclude(Include.NON_NULL) @JsonSerialize(converter = DurationToNumberConverter.class) Duration seekTime;

  @JsonPOJOBuilder(withPrefix = "")
  public static class StreamStateBuilder implements StreamStateBuilderMixin {
  }

  private interface StreamStateBuilderMixin {
    @JsonDeserialize(converter = NumberToDurationConverter.class)
    StreamStateBuilder mediaDuration(Duration mediaDuration);
    @JsonDeserialize(converter = NumberToInstantConverter.class)
    StreamStateBuilder startTime(Instant startTime);
    @JsonDeserialize(converter = InstantToNumberConverter.class)
    StreamStateBuilder endTime(Instant endTime);
    @JsonDeserialize(converter = NumberToDurationConverter.class)
    StreamStateBuilder seekTime(Duration seekTime);
  }
}
