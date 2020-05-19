package net.akaritakai.stream.handler.stream;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.akaritakai.stream.models.stream.request.StreamPauseRequest;
import net.akaritakai.stream.streamer.Streamer;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the "POST /stream/pause" command.
 */
public class PauseCommandHandler implements Handler<RoutingContext> {
  private static final Logger LOG = LoggerFactory.getLogger(PauseCommandHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final String _key;
  private final Streamer _streamer;

  public PauseCommandHandler(Streamer streamer, String key) {
    _streamer = streamer;
    _key = key;
  }

  @Override
  public void handle(RoutingContext event) {
    try {
      byte[] payload = event.getBody().getBytes();
      StreamPauseRequest request = OBJECT_MAPPER.readValue(payload, StreamPauseRequest.class);
      validateRequest(request);
      handleRequest(request, event.response());
    } catch (Exception e) {
      LOG.warn("Got invalid request", e);
      handleInvalidRequest(event.response());
    }
  }

  private void validateRequest(StreamPauseRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  private void handleRequest(StreamPauseRequest request, HttpServerResponse response) {
    // Check authorization
    boolean authorized = Objects.equals(request.getKey(), _key);
    if (authorized) {
      handleAuthorized(request, response);
    } else {
      LOG.warn("Got invalid key. Expected = {}, Actual = {}.", _key, request.getKey());
      handleUnauthorized(response);
    }
  }

  private void handleAuthorized(StreamPauseRequest request, HttpServerResponse response) {
    _streamer.pauseStream(request)
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
    String message = "Stream cannot be paused. Reason:\n" + Throwables.getStackTraceAsString(t);
    response.setStatusCode(409); // Conflict
    response.putHeader("Cache-Control", "no-store");
    response.putHeader("Content-Length", String.valueOf(message.length()));
    response.putHeader("Content-Type", "text/plain");
    response.end(message);
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "Paused the stream";
    response.setStatusCode(200); // OK
    response.putHeader("Cache-Control", "no-store");
    response.putHeader("Content-Length", String.valueOf(message.length()));
    response.putHeader("Content-Type", "text/plain");
    response.end(message);
  }
}
