package net.akaritakai.stream.models.stream.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.akaritakai.stream.models.stream.StreamEntry;

import java.util.List;

@Value
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = StreamDirResponse.StreamDirResponseBuilder.class)
public class StreamDirResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<StreamEntry> entries;

    @JsonPOJOBuilder(withPrefix = "")
    public static class StreamDirResponseBuilder {
    }
}
