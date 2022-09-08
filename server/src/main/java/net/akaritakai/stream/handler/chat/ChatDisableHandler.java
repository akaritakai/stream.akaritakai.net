package net.akaritakai.stream.handler.chat;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.chat.ChatManager;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.chat.commands.ChatDisableRequest;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the "POST /chat/disable" command.
 */
public class ChatDisableHandler extends AbstractHandler<ChatDisableRequest> {
  private static final Logger LOG = LoggerFactory.getLogger(ChatDisableHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final ChatManager _chat;

  public ChatDisableHandler(ChatManager chat, CheckAuth checkAuth) {
    super(ChatDisableRequest.class, checkAuth);
    _chat = chat;
  }
  @Override
  protected void validateRequest(ChatDisableRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  protected void handleAuthorized(ChatDisableRequest request, HttpServerResponse response) {
    _chat.disableChat(request)
        .onSuccess(event -> handleSuccess(response))
        .onFailure(t -> handleFailure(response, t));
  }

  protected void handleFailure(HttpServerResponse response, Throwable t) {
    handleFailure("Chat cannot be disabled.", response, t);
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "Disabled the chat";
    handleSuccess(message, "text/plain", response);
  }
}
