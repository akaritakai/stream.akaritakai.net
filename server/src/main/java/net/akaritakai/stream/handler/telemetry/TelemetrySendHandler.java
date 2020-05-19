package net.akaritakai.stream.handler.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.RoutingContext;
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
    InetAddress ipAddress = getIpAddressFromRequest(context.request());
    ServerWebSocket socket = context.request().upgrade();
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

  public InetAddress getIpAddressFromRequest(HttpServerRequest request) {
    try {
      String headerValue = request.getHeader("X-Forwarded-For");
      if (headerValue == null) {
        return InetAddress.getByName(request.remoteAddress().host());
      }
      String[] values = headerValue.split(",");
      if (values.length >= 2) {
        // The last value will be CloudFront's IP
        // The 2nd to last will be the IP talking to CloudFront
        return InetAddress.getByName(values[values.length - 2].trim());
      } else if (values.length == 1) {
        return InetAddress.getByName(values[0]); // Shouldn't be possible, but might happen in a dev setup
      } else {
        return InetAddress.getByName(request.remoteAddress().host()); // Dev setup
      }
    } catch (Exception e) {
      return null; // No valid IP address found
    }
  }

  public void validateTelemetryEventRequest(TelemetryEventRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notNull(request.getId(), "id cannot be null");
  }
}
