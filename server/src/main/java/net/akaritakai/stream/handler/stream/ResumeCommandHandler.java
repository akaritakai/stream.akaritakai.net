package net.akaritakai.stream.handler.stream;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.stream.request.StreamResumeRequest;
import net.akaritakai.stream.streamer.Streamer;
import org.apache.commons.lang3.Validate;


/**
 * Handles the "POST /stream/resume" command.
 */
public class ResumeCommandHandler extends AbstractHandler<StreamResumeRequest> {
  private final Streamer _streamer;

  public ResumeCommandHandler(Streamer streamer, CheckAuth authCheck) {
    super(StreamResumeRequest.class, authCheck);
    _streamer = streamer;
  }

  @Override
  protected void validateRequest(StreamResumeRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
    Validate.isTrue(request.getDelay() == null || !request.getDelay().isNegative(),
        "delay cannot be negative");
    Validate.isTrue(request.getSeekTime() == null || !request.getSeekTime().isNegative(),
        "seek time cannot be negative");
  }

  protected void handleAuthorized(HttpServerRequest httpRequest, StreamResumeRequest request, HttpServerResponse response) {
    _streamer.resumeStream(request)
        .onSuccess(event -> handleSuccess(response))
        .onFailure(t -> handleFailure(response, t));
  }

  private void handleFailure(HttpServerResponse response, Throwable t) {
    handleFailure("Stream cannot be resumed.", response, t);
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "Resumed the stream";
    handleSuccess(message, TEXT_PLAIN, response);
  }
}
