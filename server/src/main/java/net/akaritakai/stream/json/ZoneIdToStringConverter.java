package net.akaritakai.stream.json;

import java.time.ZoneId;

import com.fasterxml.jackson.databind.util.StdConverter;


public class ZoneIdToStringConverter extends StdConverter<ZoneId, String> {
  @Override
  public String convert(ZoneId value) {
    return value.toString();
  }
}
