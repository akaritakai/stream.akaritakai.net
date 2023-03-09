package net.akaritakai.stream.handler.info;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.log.DashboardLogAppender;
import net.akaritakai.stream.models.info.LogFetchRequest;
import org.apache.commons.lang3.Validate;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LogFetchHandler extends AbstractHandler<LogFetchRequest>
        implements DashboardLogAppender.DashboardLogListener {

    private volatile CompletableFuture<byte[]> _waitForBytes = new CompletableFuture<>();

    public LogFetchHandler(Vertx vertx, CheckAuth checkAuth) {
        super(LogFetchRequest.class, checkAuth);
        DashboardLogAppender.getOutputStream().addListener(bytes -> vertx.runOnContext(event -> {
            acceptLog(bytes);
        }));
    }

    @Override
    protected void validateRequest(LogFetchRequest request) {
        Validate.notNull(request, "request cannot be null");
        Validate.notEmpty(request.getKey(), "key cannot be null/empty");
    }

    @Override
    protected void handleAuthorized(HttpServerRequest httpRequest, LogFetchRequest request, HttpServerResponse response) {
        try {
            response.setStatusCode(200)
                    .setChunked(true)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .putHeader(HttpHeaders.CACHE_CONTROL, "no-store");
            _waitForBytes.thenAccept(new Consumer<byte[]>() {
                private boolean end;
                @Override
                public void accept(byte[] bytes) {
                    if (httpRequest.isEnded()) {
                        end = true;
                    }
                    if (end) {
                        return;
                    }
                    try {
                        if (bytes != null && bytes.length > 0) {
                            response.write(Buffer.buffer(bytes)).onFailure(event -> {
                                end = true;
                                response.close();
                            }).onSuccess(event -> {
                            });
                        }
                        _waitForBytes.thenAccept(this);
                        return;
                    } catch (Exception e) {
                        LOG.info("Error", e);
                    }
                    response.close();
                }
            });
            LOG.info("log listener from {}", httpRequest.remoteAddress());
        } catch (Exception e) {
            handleFailure("Failed", response, e);
        }
    }

    @Override
    public void acceptLog(byte[] data) {
        CompletableFuture<byte[]> old = _waitForBytes;
        _waitForBytes = new CompletableFuture<>();
        old.complete(data);
    }
}
