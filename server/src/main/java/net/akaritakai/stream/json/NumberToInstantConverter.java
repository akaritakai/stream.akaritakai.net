package net.akaritakai.stream.json;

import java.time.Instant;

import com.fasterxml.jackson.databind.util.StdConverter;


public class NumberToInstantConverter extends StdConverter<Number, Instant> {
  public static final NumberToInstantConverter INSTANCE = new NumberToInstantConverter();
  @Override
  public Instant convert(Number value) {
    return Instant.ofEpochMilli(value.longValue());
  }
}
