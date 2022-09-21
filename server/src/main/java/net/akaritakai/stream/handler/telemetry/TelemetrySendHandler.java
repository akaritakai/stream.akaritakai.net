package net.akaritakai.stream.handler.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.RoutingContext;
import net.akaritakai.stream.handler.Util;
import net.akaritakai.stream.models.telemetry.TelemetryEvent;
import net.akaritakai.stream.models.telemetry.TelemetryEventRequest;
import net.akaritakai.stream.telemetry.TelemetryStore;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;


/**
 * Handles websocket requests to "/telemetry"
 */
public class TelemetrySendHandler implements Handler<RoutingContext> {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final TelemetryStore _store;

  public TelemetrySendHandler(TelemetryStore store) {
    _store = store;
  }

  @Override
  public void handle(RoutingContext context) {
    InetAddress ipAddress = Util.getIpAddressFromRequest(context.request());
    context.request().toWebSocket().onFailure(context::fail).onSuccess(socket -> handle(context, ipAddress, socket));
  }

  private void handle(RoutingContext context, InetAddress ipAddress, ServerWebSocket socket) {
    context.request().resume();
    try {
      socket.textMessageHandler(message -> {
        try {
          TelemetryEventRequest request = OBJECT_MAPPER.readValue(message, TelemetryEventRequest.class);
          validateTelemetryEventRequest(request);
          TelemetryEvent event = TelemetryEvent.builder()
              .request(request)
              .ipAddress(ipAddress)
              .build();
          _store.storeEvent(event);
        } catch (Exception e) {
          socket.close((short) 400, "Bad request");
        }
      });
    } catch (Exception e) {
      socket.close((short) 500, "Unable to process telemetry request");
    }
  }

  public void validateTelemetryEventRequest(TelemetryEventRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notNull(request.getId(), "id cannot be null");
  }
}
