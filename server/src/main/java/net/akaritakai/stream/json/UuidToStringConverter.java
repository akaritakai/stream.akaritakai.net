package net.akaritakai.stream.json;

import java.util.UUID;

import com.fasterxml.jackson.databind.util.StdConverter;


public class UuidToStringConverter extends StdConverter<UUID, String> {
  @Override
  public String convert(UUID value) {
    return value.toString();
  }
}
