package net.akaritakai.stream.json;

import java.net.InetAddress;

import com.fasterxml.jackson.databind.util.StdConverter;


public class InetAddressToStringConverter extends StdConverter<InetAddress, String> {
  @Override
  public String convert(InetAddress value) {
    return value.getHostAddress();
  }
}
