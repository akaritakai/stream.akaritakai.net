package net.akaritakai.stream.models.quartz;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;
import net.akaritakai.stream.json.*;
import net.akaritakai.stream.models.stream.StreamMetadata;
import org.quartz.TimeOfDay;

import java.time.Duration;
import java.time.Instant;
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
    Duration interval;

    Integer repeatCount;

    String cronExpression;

    @JsonSerialize(converter = ZoneIdToStringConverter.class)
    ZoneId timeZone;

    Boolean preserveHourOfDayAcrossDaylightSavings;
    Boolean skipDayIfHourDoesNotExist;

    Set<Integer> daysOfWeek;

    @JsonSerialize(converter = TimeOfDayToNumberConverter.class)
    TimeOfDay startTimeOfDay;

    @JsonSerialize(converter = TimeOfDayToNumberConverter.class)
    TimeOfDay endTimeOfDay;

    Integer misfireInstruction;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ScheduleEntryBuilder implements ScheduleEntryBuilderMixin {
    }

    private interface ScheduleEntryBuilderMixin {
        @JsonDeserialize(converter = NumberToDurationConverter.class)
        ScheduleEntryBuilder interval(Duration duration);

        @JsonDeserialize(converter = StringToZoneIdConverter.class)
        ScheduleEntryBuilder timeZone(ZoneId zoneId);

        @JsonDeserialize(converter = NumberToTimeOfDayConverter.class)
        ScheduleEntryBuilder startTimeOfDay(TimeOfDay timeOfDay);

        @JsonDeserialize(converter = NumberToTimeOfDayConverter.class)
        ScheduleEntryBuilder endTimeOfDay(TimeOfDay timeOfDay);
    }
}
