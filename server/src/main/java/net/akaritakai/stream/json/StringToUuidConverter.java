package net.akaritakai.stream.json;

import java.util.UUID;

import com.fasterxml.jackson.databind.util.StdConverter;


public class StringToUuidConverter extends StdConverter<String, UUID> {
  @Override
  public UUID convert(String value) {
    return UUID.fromString(value);
  }
}
