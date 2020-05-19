package net.akaritakai.stream.models.telemetry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
@JsonDeserialize(builder = TelemetryFetchRequest.TelemetryFetchRequestBuilder.class)
public class TelemetryFetchRequest {
  String key;

  @JsonPOJOBuilder(withPrefix = "")
  public static class TelemetryFetchRequestBuilder {
  }
}
