package net.akaritakai.stream.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHandler<REQUEST> implements Handler<RoutingContext> {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected final Class<REQUEST> requestClass;

    protected AbstractHandler(Class<REQUEST> requestClass) {
        this.requestClass = requestClass;
    }

    @Override
    public void handle(RoutingContext event) {
        try {
            byte[] payload = event.getBody().getBytes();
            REQUEST request = OBJECT_MAPPER.readValue(payload, requestClass);
            validateRequest(request);
            handleRequest(request, event.response());
        } catch (Exception e) {
            LOG.warn("Got invalid request", e);
            handleInvalidRequest(event.response());
        }
    }

    protected abstract void validateRequest(REQUEST request);

    protected abstract boolean isAuthorized(REQUEST request);

    private void handleRequest(REQUEST request, HttpServerResponse response) {
        // Check authorization
        boolean authorized = isAuthorized(request);
        if (authorized) {
            handleAuthorized(request, response);
        } else {
            handleUnauthorized(response);
        }
    }

    protected abstract void handleAuthorized(REQUEST request, HttpServerResponse response);

    protected void handleUnauthorized(HttpServerResponse response) {
        // Return an Unauthorized response
        String message = "Unauthorized";
        response.setStatusCode(401); // Unauthorized
        response.putHeader("Cache-Control", "no-store");
        response.putHeader("Content-Length", String.valueOf(message.length()));
        response.putHeader("Content-Type", "text/plain");
        response.end(message);
    }

    protected void handleInvalidRequest(HttpServerResponse response) {
        String message = "Invalid request";
        response.setStatusCode(400); // Bad request
        response.putHeader("Cache-Control", "no-store");
        response.putHeader("Content-Length", String.valueOf(message.length()));
        response.putHeader("Content-Type", "text/plain");
        response.end(message);
    }

    protected Void handleFailure(String message, HttpServerResponse response, Throwable t) {
        message = message + " Reason:\n" + Throwables.getStackTraceAsString(t);
        response.setStatusCode(409); // Conflict
        response.putHeader("Cache-Control", "no-store");
        response.putHeader("Content-Length", String.valueOf(message.length()));
        response.putHeader("Content-Type", "text/plain");
        response.end(message);
        return null;
    }

    protected void handleSuccess(String message, String contentType, HttpServerResponse response) {
        response.setStatusCode(200); // OK
        response.putHeader("Cache-Control", "no-store");
        response.putHeader("Content-Length", String.valueOf(message.length()));
        response.putHeader("Content-Type", contentType);
        response.end(message);
    }
}
