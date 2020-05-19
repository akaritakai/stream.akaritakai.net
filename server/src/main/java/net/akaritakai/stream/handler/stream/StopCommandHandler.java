package net.akaritakai.stream.handler.stream;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.akaritakai.stream.models.stream.request.StreamStopRequest;
import net.akaritakai.stream.streamer.Streamer;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the "POST /stream/stop" command.
 */
public class StopCommandHandler implements Handler<RoutingContext> {
  private static final Logger LOG = LoggerFactory.getLogger(StopCommandHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final String _key;
  private final Streamer _streamer;

  public StopCommandHandler(Streamer streamer, String key) {
    _streamer = streamer;
    _key = key;
  }

  @Override
  public void handle(RoutingContext event) {
    try {
      byte[] payload = event.getBody().getBytes();
      StreamStopRequest request = OBJECT_MAPPER.readValue(payload, StreamStopRequest.class);
      validateRequest(request);
      handleRequest(request, event.response());
    } catch (Exception e) {
      LOG.warn("Got invalid request", e);
      handleInvalidRequest(event.response());
    }
  }

  private void validateRequest(StreamStopRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  private void handleRequest(StreamStopRequest request, HttpServerResponse response) {
    // Check authorization
    boolean authorized = Objects.equals(request.getKey(), _key);
    if (authorized) {
      handleAuthorized(request, response);
    } else {
      LOG.warn("Got invalid key. Expected = {}, Actual = {}.", _key, request.getKey());
      handleUnauthorized(response);
    }
  }

  private void handleAuthorized(StreamStopRequest request, HttpServerResponse response) {
    _streamer.stopStream(request)
        .onSuccess(event -> handleSuccess(response))
        .onFailure(t -> handleFailure(response, t));
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
    String message = "Stream cannot be stopped. Reason:\n" + Throwables.getStackTraceAsString(t);
    response.setStatusCode(409); // Conflict
    response.putHeader("Cache-Control", "no-store");
    response.putHeader("Content-Length", String.valueOf(message.length()));
    response.putHeader("Content-Type", "text/plain");
    response.end(message);
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "Stopped the stream";
    response.setStatusCode(200); // OK
    response.putHeader("Cache-Control", "no-store");
    response.putHeader("Content-Length", String.valueOf(message.length()));
    response.putHeader("Content-Type", "text/plain");
    response.end(message);
  }
}
