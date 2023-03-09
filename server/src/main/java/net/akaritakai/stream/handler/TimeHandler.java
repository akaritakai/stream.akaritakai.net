package net.akaritakai.stream.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

import java.time.Instant;

/**
 * Handles the "GET /time" command.
 */
public class TimeHandler implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext event) {
    String message = String.valueOf(Instant.now().toEpochMilli());
    event.response().setStatusCode(200); // OK
    event.response().putHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    event.response().putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(message.length()));
    event.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
    event.response().end(message);
  }
}
