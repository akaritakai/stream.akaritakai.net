package net.akaritakai.stream.models.quartz;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
@JsonDeserialize(builder = CalendarEntry.CalendarEntryBuilder.class)
public class CalendarEntry {
    @JsonInclude(JsonInclude.Include.NON_NULL) String name;

    CalendarEntry baseCalendar;

    String description;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CalendarEntryBuilder {
    }
}
