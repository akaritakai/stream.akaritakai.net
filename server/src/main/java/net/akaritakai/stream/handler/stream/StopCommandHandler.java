package net.akaritakai.stream.handler.stream;

import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.stream.request.StreamStopRequest;
import net.akaritakai.stream.streamer.Streamer;
import org.apache.commons.lang3.Validate;

/**
 * Handles the "POST /stream/stop" command.
 */
public class StopCommandHandler extends AbstractHandler<StreamStopRequest> {
  private final Streamer _streamer;

  public StopCommandHandler(Streamer streamer, CheckAuth authCheck) {
    super(StreamStopRequest.class, authCheck);
    _streamer = streamer;
  }

  protected void validateRequest(StreamStopRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  protected void handleAuthorized(StreamStopRequest request, HttpServerResponse response) {
    _streamer.stopStream(request)
        .onSuccess(event -> handleSuccess(response))
        .onFailure(t -> handleFailure(response, t));
  }

  private void handleFailure(HttpServerResponse response, Throwable t) {
    handleFailure("Stream cannot be stopped.", response, t);
    response.setStatusCode(409); // Conflict
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "Stopped the stream";
    handleSuccess(message, TEXT_PLAIN, response);
  }
}
