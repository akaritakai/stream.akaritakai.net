package net.akaritakai.stream.json;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.net.URL;


public class URLToStringConverter extends StdConverter<URL, String> {
  @Override
  public String convert(URL value) {
    return value.toString();
  }
}
