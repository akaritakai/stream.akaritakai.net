package net.akaritakai.stream.json;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.fasterxml.jackson.databind.util.StdConverter;


public class StringToInetAddressConverter extends StdConverter<String, InetAddress> {
  @Override
  public InetAddress convert(String value) {
    try {
      return InetAddress.getByName(value);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
}
