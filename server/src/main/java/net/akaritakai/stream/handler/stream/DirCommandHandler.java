package net.akaritakai.stream.handler.stream;

import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.stream.StreamEntry;
import net.akaritakai.stream.models.stream.request.StreamDirRequest;
import net.akaritakai.stream.models.stream.response.StreamDirResponse;
import net.akaritakai.stream.streamer.Streamer;
import org.apache.commons.lang3.Validate;

import java.util.Comparator;
import java.util.Objects;

public class DirCommandHandler extends AbstractHandler<StreamDirRequest> {

    private final String _key;
    private final Streamer _streamer;

    public DirCommandHandler(Streamer streamer, String key) {
        super(StreamDirRequest.class);
        _streamer = streamer;
        _key = key;
    }

    protected void validateRequest(StreamDirRequest request) {
        Validate.notNull(request, "request cannot be null");
        Validate.notEmpty(request.getKey(), "key cannot be null/empty");
    }

    @Override
    protected boolean isAuthorized(StreamDirRequest request) {
        return Objects.equals(request.getKey(), _key);
    }

    protected void handleAuthorized(StreamDirRequest request, HttpServerResponse response) {
        _streamer.listStreams(request.getFilter()).onSuccess(list -> {
            try {
                list.sort(Comparator.comparing(StreamEntry::getMetadataName));
                StreamDirResponse dirResponse = StreamDirResponse.builder().entries(list).build();
                String responseAsString = OBJECT_MAPPER.writeValueAsString(dirResponse);
                handleSuccess(responseAsString, "application/json", response);
            } catch (Exception e) {
                handleFailure("Error serializing response", response, e);
            }
        }).onFailure(ex -> handleFailure("Directory error", response, ex));
    }
}
