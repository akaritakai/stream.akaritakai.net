package net.akaritakai.stream.models.stream.request;

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
@JsonDeserialize(builder = StreamResumeRequest.StreamResumeRequestBuilder.class)
public class StreamResumeRequest {
  String key;
  @JsonInclude(Include.NON_NULL) @JsonSerialize(converter = DurationToNumberConverter.class) Duration delay;
  @JsonInclude(Include.NON_NULL) @JsonSerialize(converter = DurationToNumberConverter.class) Duration seekTime;
  @JsonInclude(Include.NON_NULL) @JsonSerialize(converter = InstantToNumberConverter.class) Instant startAt;

  @JsonPOJOBuilder(withPrefix = "")
  public static class StreamResumeRequestBuilder implements StreamResumeRequestBuilderMixin {
  }

  private interface StreamResumeRequestBuilderMixin {
    @JsonDeserialize(converter = NumberToDurationConverter.class)
    StreamResumeRequestBuilder delay(Duration delay);
    @JsonDeserialize(converter = NumberToDurationConverter.class)
    StreamResumeRequestBuilder seekTime(Duration seekTime);
    @JsonDeserialize(converter = NumberToInstantConverter.class)
    StreamResumeRequestBuilder startAt(Instant startAt);
  }
}
