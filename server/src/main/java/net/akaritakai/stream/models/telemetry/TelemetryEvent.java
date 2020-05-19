package net.akaritakai.stream.models.telemetry;

import java.net.InetAddress;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.json.InetAddressToStringConverter;
import net.akaritakai.stream.json.StringToInetAddressConverter;


@Value
@Builder
@JsonDeserialize(builder = TelemetryEvent.TelemetryEventBuilder.class)
public class TelemetryEvent {
  TelemetryEventRequest request;
  @JsonInclude(Include.NON_NULL) @JsonSerialize(converter = InetAddressToStringConverter.class) InetAddress ipAddress;

  @JsonPOJOBuilder(withPrefix = "")
  public static class TelemetryEventBuilder implements TelemetryEventBuilderMixin {
  }

  private interface TelemetryEventBuilderMixin {
    @JsonDeserialize(converter = StringToInetAddressConverter.class)
    TelemetryEventBuilder ipAddress(InetAddress ipAddress);
  }
}
