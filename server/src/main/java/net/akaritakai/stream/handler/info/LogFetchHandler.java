package net.akaritakai.stream.handler.info;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.akaritakai.stream.log.DashboardLogAppender;
import net.akaritakai.stream.models.telemetry.TelemetryFetchRequest;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LogFetchHandler implements Handler<RoutingContext>, DashboardLogAppender.DashboardLogListener {
    private static final Logger LOG = LoggerFactory.getLogger(LogFetchHandler.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String _key;

    private volatile CompletableFuture<byte[]> _waitForBytes = new CompletableFuture<>();

    public LogFetchHandler(Vertx vertx, String key) {
        _key = key;
        DashboardLogAppender.getOutputStream().addListener(bytes -> vertx.runOnContext(event -> {
            acceptLog(bytes);
        }));
    }

    @Override
    public void handle(RoutingContext event) {
        try {
            byte[] payload = event.getBody().getBytes();
            TelemetryFetchRequest request = OBJECT_MAPPER.readValue(payload, TelemetryFetchRequest.class);
            validateRequest(request);
            handleRequest(request, event.response());
        } catch (Exception e) {
            LOG.warn("Got invalid request", e);
            handleInvalidRequest(event.response());
        }
    }

    private void validateRequest(TelemetryFetchRequest request) {
        Validate.notNull(request, "request cannot be null");
        Validate.notEmpty(request.getKey(), "key cannot be null/empty");
    }

    private void handleRequest(TelemetryFetchRequest request, HttpServerResponse response) {
        // Check authorization
        boolean authorized = Objects.equals(request.getKey(), _key);
        if (authorized) {
            handleAuthorized(response);
        } else {
            LOG.warn("Got invalid key. Expected = {}, Actual = {}.", _key, request.getKey());
            handleUnauthorized(response);
        }
    }

    private void handleAuthorized(HttpServerResponse response) {
        try {
            response.setStatusCode(200)
                    .setChunked(true)
                    .putHeader("Content-Type", "text/plain")
                    .putHeader("Cache-Control", "no-store");
            _waitForBytes.thenAccept(new Consumer<byte[]>() {
                private boolean end;
                @Override
                public void accept(byte[] bytes) {
                    if (end) {
                        return;
                    }
                    try {
                        response.write(Buffer.buffer(bytes)).onFailure(event -> {
                            end = true;
                            response.close();
                        }).onSuccess(event -> {
                        });
                        _waitForBytes.thenAccept(this);
                        return;
                    } catch (Exception e) {
                        LOG.info("Error", e);
                    }
                    response.close();
                }
            });
        } catch (Exception e) {
            handleFailure(response, e);
        }
    }

    private void handleUnauthorized(HttpServerResponse response) {
        // Return an Unauthorized response
        String message = "Unauthorized";
        response.setStatusCode(401); // Unauthorized
        response.putHeader("Cache-Control", "no-store");
        response.putHeader("Content-Length", String.valueOf(message.length()));
        response.putHeader("Content-Type", "text/plain");
        response.end(message);
    }

    private void handleInvalidRequest(HttpServerResponse response) {
        String message = "Invalid request";
        response.setStatusCode(400); // Bad request
        response.putHeader("Cache-Control", "no-store");
        response.putHeader("Content-Length", String.valueOf(message.length()));
        response.putHeader("Content-Type", "text/plain");
        response.end(message);
    }

    private void handleFailure(HttpServerResponse response, Throwable t) {
        String message = "Log fetch failure. Reason:\n" + Throwables.getStackTraceAsString(t);
        response.setStatusCode(409); // Conflict
        response.putHeader("Cache-Control", "no-store");
        response.putHeader("Content-Length", String.valueOf(message.length()));
        response.putHeader("Content-Type", "text/plain");
        response.end(message);
    }

    private void handleSuccess(HttpServerResponse response, String message) {
        response.setStatusCode(200); // OK
        response.putHeader("Cache-Control", "no-store");
        response.putHeader("Content-Length", String.valueOf(message.length()));
        response.putHeader("Content-Type", "text/plain");
        response.end(message);
    }

    @Override
    public void acceptLog(byte[] data) {
        CompletableFuture<byte[]> old = _waitForBytes;
        _waitForBytes = new CompletableFuture<>();
        old.complete(data);
    }
}
