package net.akaritakai.stream.json;

import java.time.ZoneId;

import com.fasterxml.jackson.databind.util.StdConverter;


public class StringToZoneIdConverter extends StdConverter<String, ZoneId> {
  @Override
  public ZoneId convert(String value) {
    return ZoneId.of(value);
  }
}
