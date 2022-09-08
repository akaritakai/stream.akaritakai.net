package net.akaritakai.stream.models.quartz.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

@Value
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = ListCalendarsResponse.ListCalendarsResponseBuilder.class)
public class ListCalendarsResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<String> calendars;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ListCalendarsResponseBuilder {
    }
}
