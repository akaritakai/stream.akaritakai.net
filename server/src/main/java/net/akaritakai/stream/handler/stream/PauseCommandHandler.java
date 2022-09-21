package net.akaritakai.stream.handler.stream;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.stream.request.StreamPauseRequest;
import net.akaritakai.stream.streamer.Streamer;
import org.apache.commons.lang3.Validate;

/**
 * Handles the "POST /stream/pause" command.
 */
public class PauseCommandHandler extends AbstractHandler<StreamPauseRequest> {
  private final Streamer _streamer;

  public PauseCommandHandler(Streamer streamer, CheckAuth checkAuth) {
    super(StreamPauseRequest.class, checkAuth);
    _streamer = streamer;
  }

  protected void validateRequest(StreamPauseRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  protected void handleAuthorized(HttpServerRequest httpRequest, StreamPauseRequest request, HttpServerResponse response) {
    _streamer.pauseStream(request)
        .onSuccess(event -> handleSuccess(response))
        .onFailure(t -> handleFailure(response, t));
  }

  private void handleFailure(HttpServerResponse response, Throwable t) {
    handleFailure("Stream cannot be paused.", response, t);
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "Paused the stream";
    handleSuccess(message, TEXT_PLAIN, response);
  }
}
