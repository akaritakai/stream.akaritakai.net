package net.akaritakai.stream.models.quartz;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = KeyEntry.KeyEntryBuilder.class)
public class KeyEntry {
    @JsonInclude(JsonInclude.Include.NON_NULL) String name;

    String group;

    @JsonPOJOBuilder(withPrefix = "")
    public static class KeyEntryBuilder {
    }
}
