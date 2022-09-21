package net.akaritakai.stream.handler.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.telemetry.TelemetryFetchRequest;
import net.akaritakai.stream.telemetry.TelemetryStore;
import org.apache.commons.lang3.Validate;

/**
 * Handles the "POST /telemetry/fetch" command.
 */
public class TelemetryFetchHandler extends AbstractHandler<TelemetryFetchRequest> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final TelemetryStore _store;

  public TelemetryFetchHandler(TelemetryStore store, CheckAuth authCheck) {
    super(TelemetryFetchRequest.class, authCheck);
    _store = store;
  }

  protected void validateRequest(TelemetryFetchRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  protected void handleAuthorized(HttpServerRequest httpRequest, TelemetryFetchRequest request, HttpServerResponse response) {
    try {
      String message = OBJECT_MAPPER.writeValueAsString(_store.getTelemetry());
      handleSuccess(message, APPLICATION_JSON, response);
    } catch (Exception e) {
      handleFailure("Fail", response, e);
    }
  }
}
