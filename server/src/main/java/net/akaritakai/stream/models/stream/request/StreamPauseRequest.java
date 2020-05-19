package net.akaritakai.stream.models.stream.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
@JsonDeserialize(builder = StreamPauseRequest.StreamPauseRequestBuilder.class)
public class StreamPauseRequest {
  String key;

  @JsonPOJOBuilder(withPrefix = "")
  public static class StreamPauseRequestBuilder {
  }
}
