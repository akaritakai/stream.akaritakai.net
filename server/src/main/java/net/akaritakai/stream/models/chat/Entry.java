package net.akaritakai.stream.models.chat;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.Map;


@Value
@Builder
@JsonDeserialize(builder = Entry.EntryBuilder.class)
public class Entry implements Map.Entry<String, String> {
  String key;
  String value;

  @Override
  public String setValue(String value) {
    throw new UnsupportedOperationException();
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class EntryBuilder {
  }
}
