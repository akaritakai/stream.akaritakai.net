package net.akaritakai.stream.json;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.quartz.TimeOfDay;

import java.time.Instant;


public class NumberToTimeOfDayConverter extends StdConverter<Number, TimeOfDay> {
  @Override
  public TimeOfDay convert(Number value) {
    long seconds = value.longValue() / 1000L;
    long minute = seconds / 60; seconds -= minute * 60;
    long hour = minute / 60; minute -= hour * 60;

    return TimeOfDay.hourMinuteAndSecondOfDay((int) hour, (int) minute, (int) seconds);
  }
}
