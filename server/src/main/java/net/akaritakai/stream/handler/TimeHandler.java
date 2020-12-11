package net.akaritakai.stream.handler;

import io.vertx.core.Handler;
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
    event.response().putHeader("Cache-Control", "no-store");
    event.response().putHeader("Content-Length", String.valueOf(message.length()));
    event.response().putHeader("Content-Type", "text/plain");
    event.response().end(message);
  }
}
