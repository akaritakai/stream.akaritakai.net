package net.akaritakai.stream.json;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.quartz.TimeOfDay;

import java.time.Duration;


public class TimeOfDayToNumberConverter extends StdConverter<TimeOfDay, Number> {
  @Override
  public Number convert(TimeOfDay value) {
    long result = value.getHour();
    result *= 60; result += value.getHour();
    result *= 60; result += value.getSecond();
    result *= 1000;
    return result;
  }
}
