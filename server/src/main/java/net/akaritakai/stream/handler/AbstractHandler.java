package net.akaritakai.stream.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.akaritakai.stream.CheckAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHandler<REQUEST> implements Handler<RoutingContext> {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    protected static final String TEXT_PLAIN = "text/plain";
    protected static final String APPLICATION_JSON = "application/json";

    protected final Class<REQUEST> _requestClass;
    private final CheckAuth _checkAuth;

    protected AbstractHandler(Class<REQUEST> requestClass, CheckAuth checkAuth) {
        _requestClass = requestClass;
        _checkAuth = checkAuth;
    }

    @Override
    public void handle(RoutingContext event) {
        try {
            byte[] payload = event.getBody().getBytes();
            REQUEST request = OBJECT_MAPPER.readValue(payload, _requestClass);
            validateRequest(request);
            handleRequest(request, event.response());
        } catch (Exception e) {
            LOG.warn("Got invalid request", e);
            handleInvalidRequest(event.response());
        }
    }

    protected abstract void validateRequest(REQUEST request);

    protected boolean isAuthorized(REQUEST request) {
        return _checkAuth.isAuthorizedRequest(request);
    }

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
        handleResponse(401, message, TEXT_PLAIN, response);
    }

    protected void handleInvalidRequest(HttpServerResponse response) {
        String message = "Invalid request";
        handleResponse(400, message, TEXT_PLAIN, response);
    }

    protected Void handleFailure(String message, HttpServerResponse response, Throwable t) {
        message = message + " Reason:\n" + Throwables.getStackTraceAsString(t);
        handleResponse(409, message, TEXT_PLAIN, response);
        return null;
    }

    protected void handleSuccess(String message, String contentType, HttpServerResponse response) {
        handleResponse(200, message, contentType, response);
    }

    protected void handleResponse(int statusCode, String message, String contentType, HttpServerResponse response) {
        response.setStatusCode(statusCode); // OK
        response.putHeader("Cache-Control", "no-store");
        response.putHeader("Content-Length", String.valueOf(message.length()));
        response.putHeader("Content-Type", contentType);
        response.end(message);
    }
}
