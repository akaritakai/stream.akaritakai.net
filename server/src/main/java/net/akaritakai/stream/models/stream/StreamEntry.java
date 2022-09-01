package net.akaritakai.stream.models.stream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = StreamEntry.StreamEntryBuilder.class)
public class StreamEntry {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String metadataName;

    boolean metadataLive;

    @JsonPOJOBuilder(withPrefix = "")
    public static class StreamEntryBuilder {
    }
}
