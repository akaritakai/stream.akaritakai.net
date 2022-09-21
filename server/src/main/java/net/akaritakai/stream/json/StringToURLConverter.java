package net.akaritakai.stream.json;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.net.MalformedURLException;
import java.net.URL;


public class StringToURLConverter extends StdConverter<String, URL> {
  @Override
  public URL convert(String value) {
    try {
      return new URL(value);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
