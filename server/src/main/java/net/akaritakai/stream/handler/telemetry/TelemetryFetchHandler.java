package net.akaritakai.stream.handler.telemetry;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.akaritakai.stream.models.telemetry.TelemetryFetchRequest;
import net.akaritakai.stream.telemetry.TelemetryStore;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the "POST /telemetry/fetch" command.
 */
public class TelemetryFetchHandler implements Handler<RoutingContext> {
  private static final Logger LOG = LoggerFactory.getLogger(TelemetryFetchHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final TelemetryStore _store;
  private final String _key;

  public TelemetryFetchHandler(TelemetryStore store, String key) {
    _store = store;
    _key = key;
  }

  @Override
  public void handle(RoutingContext event) {
    try {
      byte[] payload = event.getBody().getBytes();
      TelemetryFetchRequest request = OBJECT_MAPPER.readValue(payload, TelemetryFetchRequest.class);
      validateRequest(request);
      handleRequest(request, event.response());
    } catch (Exception e) {
      LOG.warn("Got invalid request", e);
      handleInvalidRequest(event.response());
    }
  }

  private void validateRequest(TelemetryFetchRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  private void handleRequest(TelemetryFetchRequest request, HttpServerResponse response) {
    // Check authorization
    boolean authorized = Objects.equals(request.getKey(), _key);
    if (authorized) {
      handleAuthorized(response);
    } else {
      LOG.warn("Got invalid key. Expected = {}, Actual = {}.", _key, request.getKey());
      handleUnauthorized(response);
    }
  }

  private void handleAuthorized(HttpServerResponse response) {
    try {
      String message = OBJECT_MAPPER.writeValueAsString(_store.getTelemetry());
      handleSuccess(response, message);
    } catch (Exception e) {
      handleFailure(response, e);
    }
  }

  private void handleUnauthorized(HttpServerResponse response) {
    // Return an Unauthorized response
    String message = "Unauthorized";
    response.setStatusCode(401); // Unauthorized
    response.putHeader("Cache-Control", "no-store");
    response.putHeader("Content-Length", String.valueOf(message.length()));
    response.putHeader("Content-Type", "text/plain");
    response.end(message);
  }

  private void handleInvalidRequest(HttpServerResponse response) {
    String message = "Invalid request";
    response.setStatusCode(400); // Bad request
    response.putHeader("Cache-Control", "no-store");
    response.putHeader("Content-Length", String.valueOf(message.length()));
    response.putHeader("Content-Type", "text/plain");
    response.end(message);
  }

  private void handleFailure(HttpServerResponse response, Throwable t) {
    String message = "Chat cannot be cleared. Reason:\n" + Throwables.getStackTraceAsString(t);
    response.setStatusCode(409); // Conflict
    response.putHeader("Cache-Control", "no-store");
    response.putHeader("Content-Length", String.valueOf(message.length()));
    response.putHeader("Content-Type", "text/plain");
    response.end(message);
  }

  private void handleSuccess(HttpServerResponse response, String message) {
    response.setStatusCode(200); // OK
    response.putHeader("Cache-Control", "no-store");
    response.putHeader("Content-Length", String.valueOf(message.length()));
    response.putHeader("Content-Type", "text/plain");
    response.end(message);
  }
}
