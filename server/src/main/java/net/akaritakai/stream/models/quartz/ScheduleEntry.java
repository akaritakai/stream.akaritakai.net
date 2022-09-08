package net.akaritakai.stream.models.quartz;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.json.*;
import org.quartz.TimeOfDay;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Set;

@Value
@Builder
@JsonDeserialize(builder = ScheduleEntry.ScheduleEntryBuilder.class)
public class ScheduleEntry {

    public enum ScheduleType {
        SIMPLE,
        CALENDAR,
        CRON,
    }

    ScheduleType type;

    @JsonSerialize(converter = DurationToNumberConverter.class)
    @JsonDeserialize(converter = NumberToDurationConverter.class)
    Duration interval;

    Integer repeatCount;

    String cronExpression;

    @JsonSerialize(converter = ZoneIdToStringConverter.class)
    @JsonDeserialize(converter = StringToZoneIdConverter.class)
    ZoneId timeZone;

    Boolean preserveHourOfDayAcrossDaylightSavings;
    Boolean skipDayIfHourDoesNotExist;

    Set<Integer> daysOfWeek;

    @JsonSerialize(converter = TimeOfDayToNumberConverter.class)
    @JsonDeserialize(converter = NumberToTimeOfDayConverter.class)
    TimeOfDay startTimeOfDay;

    @JsonSerialize(converter = TimeOfDayToNumberConverter.class)
    @JsonDeserialize(converter = NumberToTimeOfDayConverter.class)
    TimeOfDay endTimeOfDay;

    Integer misfireInstruction;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ScheduleEntryBuilder {
    }
}
