package net.akaritakai.stream.telemetry;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.akaritakai.stream.models.telemetry.TelemetryEvent;


public class TelemetryStore {
  private static final int MAX_NUMBER_OF_USERS = 1000;
  private static final Duration MAX_USER_TTL = Duration.ofMinutes(1);

  private final Cache<UUID, TelemetryEvent> _cache = CacheBuilder.newBuilder()
      .expireAfterWrite(MAX_USER_TTL)
      .maximumSize(MAX_NUMBER_OF_USERS)
      .build();

  public Collection<TelemetryEvent> getTelemetry() {
    return Collections.unmodifiableCollection(_cache.asMap().values());
  }

  public void storeEvent(TelemetryEvent event) {
    _cache.put(event.getRequest().getId(), event);
  }
}
