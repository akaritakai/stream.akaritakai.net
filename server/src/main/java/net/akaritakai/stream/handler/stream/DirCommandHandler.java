package net.akaritakai.stream.handler.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.stream.StreamEntry;
import net.akaritakai.stream.models.stream.request.StreamDirRequest;
import net.akaritakai.stream.models.stream.response.StreamDirResponse;
import net.akaritakai.stream.scheduling.Utils;
import net.akaritakai.stream.streamer.Streamer;
import net.akaritakai.stream.streamer.StreamerMBean;
import org.apache.commons.lang3.Validate;

import javax.management.ObjectName;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class DirCommandHandler extends AbstractHandler<StreamDirRequest> {

    private final Vertx _vertx;
    private final StreamerMBean _streamer;

    public DirCommandHandler(ObjectName streamer, CheckAuth checkAuth, Vertx vertx) {
        super(StreamDirRequest.class, checkAuth);
        _streamer = Utils.beanProxy(streamer, StreamerMBean.class);
        _vertx = vertx;
    }

    protected void validateRequest(StreamDirRequest request) {
        Validate.notNull(request, "request cannot be null");
        Validate.notEmpty(request.getKey(), "key cannot be null/empty");
    }

    protected void handleAuthorized(HttpServerRequest httpRequest, StreamDirRequest request, HttpServerResponse response) {
        CompletableFuture
                .completedStage(request.getFilter())
                .thenApplyAsync(_streamer::listStreams)
                .thenApplyAsync(list -> list.stream()
                        .map(entry -> {
                            try {
                                return OBJECT_MAPPER.readValue(entry, StreamEntry.class);
                            } catch (JsonProcessingException e) {
                                throw new CompletionException(e);
                            }
                        })
                        .sorted(Comparator.comparing(StreamEntry::getName))
                        .collect(Collectors.toList()))
                .thenAcceptAsync(list -> {
                    try {
                        StreamDirResponse dirResponse = StreamDirResponse.builder().entries(list).build();
                        String responseAsString = OBJECT_MAPPER.writeValueAsString(dirResponse);
                        handleSuccess(responseAsString, "application/json", response);
                    } catch (JsonProcessingException e) {
                        throw new CompletionException(e);
                    }
                }, runnable -> _vertx.runOnContext(event -> runnable.run()))
                .exceptionally(ex -> {
                    _vertx.runOnContext(event -> handleFailure("Directory error", response, ex));
                    return null;
                });
    }
}
