package net.akaritakai.stream.json;

import java.time.Instant;

import com.fasterxml.jackson.databind.util.StdConverter;


public class InstantToNumberConverter extends StdConverter<Instant, Number> {
  @Override
  public Number convert(Instant value) {
    return value.toEpochMilli();
  }
}
