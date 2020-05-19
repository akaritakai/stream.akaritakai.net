package net.akaritakai.stream.models.telemetry;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.json.InstantToNumberConverter;
import net.akaritakai.stream.json.NumberToInstantConverter;
import net.akaritakai.stream.json.StringToUuidConverter;
import net.akaritakai.stream.json.StringToZoneIdConverter;
import net.akaritakai.stream.json.UuidToStringConverter;
import net.akaritakai.stream.json.ZoneIdToStringConverter;


@Value
@Builder
@JsonDeserialize(builder = TelemetryEventRequest.TelemetryEventRequestBuilder.class)
public class TelemetryEventRequest {
  @JsonSerialize(converter = UuidToStringConverter.class) UUID id;
  @JsonSerialize(converter = InstantToNumberConverter.class) Instant timestamp;

  // Environment info
  String userAgent;
  @JsonSerialize(converter = ZoneIdToStringConverter.class) ZoneId timeZone;

  // Size info
  int screenHeight;
  int screenWidth;
  int availHeight;
  int availWidth;
  @JsonInclude(JsonInclude.Include.NON_NULL) Integer rootHeight;
  @JsonInclude(JsonInclude.Include.NON_NULL) Integer rootWidth;
  @JsonInclude(JsonInclude.Include.NON_NULL) Integer streamHeight;
  @JsonInclude(JsonInclude.Include.NON_NULL) Integer streamWidth;

  // Usage info
  boolean visible;
  boolean videoFullScreen;
  boolean pageFullScreen;
  boolean chatOpened;
  boolean muted;
  @JsonInclude(JsonInclude.Include.NON_NULL) String chatNick;

  // Video info
  @JsonInclude(JsonInclude.Include.NON_NULL) String videoQuality;
  @JsonInclude(JsonInclude.Include.NON_NULL) Long clientBandwidth;

  @JsonPOJOBuilder(withPrefix = "")
  public static class TelemetryEventRequestBuilder implements TelemetryEventRequestBuilderMixin {
  }

  private interface TelemetryEventRequestBuilderMixin {
    @JsonDeserialize(converter = StringToUuidConverter.class)
    TelemetryEventRequestBuilder id(UUID id);
    @JsonDeserialize(converter = NumberToInstantConverter.class)
    TelemetryEventRequestBuilder timestamp(Instant timeStamp);
    @JsonDeserialize(converter = StringToZoneIdConverter.class)
    TelemetryEventRequestBuilder timeZone(ZoneId timeZone);
  }
}
