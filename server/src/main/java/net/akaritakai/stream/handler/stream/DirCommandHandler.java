package net.akaritakai.stream.handler.stream;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.stream.StreamEntry;
import net.akaritakai.stream.models.stream.request.StreamDirRequest;
import net.akaritakai.stream.models.stream.response.StreamDirResponse;
import net.akaritakai.stream.streamer.Streamer;
import org.apache.commons.lang3.Validate;

import java.util.Comparator;

public class DirCommandHandler extends AbstractHandler<StreamDirRequest> {

    private final Streamer _streamer;

    public DirCommandHandler(Streamer streamer, CheckAuth checkAuth) {
        super(StreamDirRequest.class, checkAuth);
        _streamer = streamer;
    }

    protected void validateRequest(StreamDirRequest request) {
        Validate.notNull(request, "request cannot be null");
        Validate.notEmpty(request.getKey(), "key cannot be null/empty");
    }

    protected void handleAuthorized(HttpServerRequest httpRequest, StreamDirRequest request, HttpServerResponse response) {
        _streamer.listStreams(request.getFilter()).onSuccess(list -> {
            try {
                list.sort(Comparator.comparing(StreamEntry::getName));
                StreamDirResponse dirResponse = StreamDirResponse.builder().entries(list).build();
                String responseAsString = OBJECT_MAPPER.writeValueAsString(dirResponse);
                handleSuccess(responseAsString, "application/json", response);
            } catch (Exception e) {
                handleFailure("Error serializing response", response, e);
            }
        }).onFailure(ex -> handleFailure("Directory error", response, ex));
    }
}
