package net.akaritakai.stream.json;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.fasterxml.jackson.databind.util.StdConverter;


public class StringToInetAddressConverter extends StdConverter<String, InetAddress> {
  @Override
  public InetAddress convert(String value) {
    // We don't want to be doing any annoying DNS lookups here
    byte[] addr = IPAddressUtil.textToNumericFormatV4(value);
    if (addr == null) {
      addr = IPAddressUtil.textToNumericFormatV6(value);
      if (addr == null) {
        throw new NumberFormatException("Expected IP address");
      }
    }
    try {
      return InetAddress.getByAddress(value, addr);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
}
