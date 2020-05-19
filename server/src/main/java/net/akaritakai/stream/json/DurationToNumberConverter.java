package net.akaritakai.stream.json;

import java.time.Duration;

import com.fasterxml.jackson.databind.util.StdConverter;


public class DurationToNumberConverter extends StdConverter<Duration, Number> {
  @Override
  public Number convert(Duration value) {
    return value.toMillis();
  }
}
