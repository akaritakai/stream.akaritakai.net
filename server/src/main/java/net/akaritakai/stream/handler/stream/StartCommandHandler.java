package net.akaritakai.stream.handler.stream;

import com.google.common.base.Throwables;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.exception.StreamStateConflictException;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.stream.request.StreamStartRequest;
import net.akaritakai.stream.streamer.Streamer;
import org.apache.commons.lang3.Validate;

/**
 * Handles the "POST /stream/start" command.
 */
public class StartCommandHandler extends AbstractHandler<StreamStartRequest> {
  private final Streamer _streamer;

  public StartCommandHandler(Streamer streamer, CheckAuth authCheck) {
    super(StreamStartRequest.class, authCheck);
    _streamer = streamer;
  }

  protected void validateRequest(StreamStartRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
    Validate.notEmpty(request.getName(), "name cannot be null/empty");
    Validate.isTrue(request.getSeekTime() == null || !request.getSeekTime().isNegative(),
        "seek time cannot be negative");
    Validate.isTrue(request.getDelay() == null || !request.getDelay().isNegative(),
        "delay cannot be negative");
  }

  protected void handleAuthorized(StreamStartRequest request, HttpServerResponse response) {
    _streamer.startStream(request)
        .onSuccess(event -> handleSuccess(response))
        .onFailure(t -> {
          if (t instanceof StreamStateConflictException) {
            handleStreamAlreadyRunning(response);
          } else {
            handleStreamCannotBeLoaded(response, t);
          }
        });
  }

  private void handleStreamAlreadyRunning(HttpServerResponse response) {
    String message = "Stream is already running";
    handleResponse(409, message, TEXT_PLAIN, response);
  }

  private void handleStreamCannotBeLoaded(HttpServerResponse response, Throwable e) {
    String message = "Stream cannot be started. Reason:\n" + Throwables.getStackTraceAsString(e);
    handleResponse(404, message, TEXT_PLAIN, response);
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "Started the stream";
    handleSuccess(message, TEXT_PLAIN, response);
  }
}
