package net.akaritakai.stream.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

/**
 * Handles the "GET /health" command.
 */
public class HealthCheckHandler implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext event) {
    String message = "OK";
    event.response().setStatusCode(200); // OK
    event.response().putHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    event.response().putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(message.length()));
    event.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
    event.response().end(message);
  }
}
