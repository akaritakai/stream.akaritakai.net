package net.akaritakai.stream.handler.stream;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.stream.request.StreamPauseRequest;
import net.akaritakai.stream.scheduling.Utils;
import net.akaritakai.stream.streamer.StreamerMBean;
import org.apache.commons.lang3.Validate;

import javax.management.ObjectName;
import java.util.concurrent.CompletableFuture;

/**
 * Handles the "POST /stream/pause" command.
 */
public class PauseCommandHandler extends AbstractHandler<StreamPauseRequest> {
  private final Vertx _vertx;
  private final StreamerMBean _streamer;

  public PauseCommandHandler(ObjectName streamer, CheckAuth checkAuth, Vertx vertx) {
    super(StreamPauseRequest.class, checkAuth);
    _streamer = Utils.beanProxy(streamer, StreamerMBean.class);
    _vertx = vertx;
  }

  protected void validateRequest(StreamPauseRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  protected void handleAuthorized(HttpServerRequest httpRequest, StreamPauseRequest request, HttpServerResponse response) {
    CompletableFuture
            .completedStage(request)
            .thenApplyAsync(Utils::writeAsString)
            .thenAcceptAsync(_streamer::pauseStream)
            .whenComplete(((unused, ex) -> {
              _vertx.runOnContext(event -> {
                if (ex == null) {
                  handleSuccess(response);
                } else {
                  handleFailure(response, ex);
                }
              });
            }));
  }

  private void handleFailure(HttpServerResponse response, Throwable t) {
    handleFailure("Stream cannot be paused.", response, t);
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "Paused the stream";
    handleSuccess(message, TEXT_PLAIN, response);
  }
}
