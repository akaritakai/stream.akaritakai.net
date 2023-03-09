package net.akaritakai.stream.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.IntSupplier;

public class RedirectHandler implements Handler<RoutingContext> {

    private static final Logger LOG = LoggerFactory.getLogger(RedirectHandler.class);

    private final IntSupplier targetPort;

    public RedirectHandler(IntSupplier targetPort) {
        this.targetPort = targetPort;
    }

    @Override
    public void handle(RoutingContext event) {
        String host = event.request().getHeader(HttpHeaders.HOST);
        int portIdx = host.lastIndexOf(':');
        if (portIdx >= 0) {
            host = host.substring(0, portIdx);
        }
        String target = "http://" + host + ":" + targetPort.getAsInt() + event.request().uri();
        LOG.debug("Redirecting to {}", target);
        event.response().setStatusCode(308); // Permanent Redirect
        event.response().putHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        event.response().putHeader(HttpHeaders.LOCATION, target);
        event.response().end();
    }
}
