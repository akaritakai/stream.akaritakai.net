package net.akaritakai.stream.handler.info;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.RoutingContext;
import net.akaritakai.stream.log.DashboardLogAppender;
import net.akaritakai.stream.models.info.LogSendRequest;
import org.apache.commons.lang3.Validate;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class LogSendHandler implements Handler<RoutingContext> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public LogSendHandler() {
    }

    @Override
    public void handle(RoutingContext context) {
        InetAddress ipAddress = getIpAddressFromRequest(context.request());
        context.request().toWebSocket().onFailure(context::fail).onSuccess(socket -> handle(context, ipAddress, socket));
    }

    private void handle(RoutingContext context, InetAddress ipAddress, ServerWebSocket socket) {
        context.request().resume();
        try {
            socket.textMessageHandler(message -> {
                try {
                    LogSendRequest request = OBJECT_MAPPER.readValue(message, LogSendRequest.class);
                    validateLogSendRequest(request);
                    DashboardLogAppender.getOutputStream().writeBytes(request.getLog().getBytes(StandardCharsets.UTF_8));
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

    public void validateLogSendRequest(LogSendRequest request) {
        Validate.notNull(request, "request cannot be null");
    }
}
