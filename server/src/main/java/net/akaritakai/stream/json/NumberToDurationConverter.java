package net.akaritakai.stream.json;

import java.time.Duration;

import com.fasterxml.jackson.databind.util.StdConverter;


public class NumberToDurationConverter extends StdConverter<Number, Duration> {
  public static final NumberToDurationConverter INSTANCE = new NumberToDurationConverter();

  @Override
  public Duration convert(Number value) {
    return Duration.ofMillis(value.longValue());
  }
}
