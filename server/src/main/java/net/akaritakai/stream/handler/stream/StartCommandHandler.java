package net.akaritakai.stream.handler.stream;

import com.google.common.base.Throwables;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.exception.StreamStateConflictException;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.stream.request.StreamStartRequest;
import net.akaritakai.stream.scheduling.Utils;
import net.akaritakai.stream.streamer.StreamerMBean;
import org.apache.commons.lang3.Validate;

import javax.management.ObjectName;
import java.util.concurrent.CompletableFuture;

/**
 * Handles the "POST /stream/start" command.
 */
public class StartCommandHandler extends AbstractHandler<StreamStartRequest> {
  private final Vertx _vertx;
  private final StreamerMBean _streamer;

  public StartCommandHandler(ObjectName streamer, CheckAuth authCheck, Vertx vertx) {
    super(StreamStartRequest.class, authCheck);
    _streamer = Utils.beanProxy(streamer, StreamerMBean.class);
    _vertx = vertx;
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

  protected void handleAuthorized(HttpServerRequest httpRequest, StreamStartRequest request, HttpServerResponse response) {
    CompletableFuture
            .completedStage(request)
            .thenApplyAsync(Utils::writeAsString)
            .thenAcceptAsync(_streamer::startStream)
            .thenRun(() -> _vertx.runOnContext(event -> handleSuccess(response)))
            .exceptionally(ex -> {
              _vertx.runOnContext(event -> {
                if (ex instanceof StreamStateConflictException) {
                  handleStreamAlreadyRunning(response);
                } else {
                  handleStreamCannotBeLoaded(response, ex);
                }
              });
              return null;
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
