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
import net.akaritakai.stream.models.chat.commands.ChatClearRequest;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the "POST /chat/clear" command.
 */
public class ChatClearHandler extends AbstractHandler<ChatClearRequest> {
  private static final Logger LOG = LoggerFactory.getLogger(ChatClearHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final ChatManager _chat;

  public ChatClearHandler(ChatManager chat, CheckAuth checkAuth) {
    super(ChatClearRequest.class, checkAuth);
    _chat = chat;
  }
  @Override
  protected void validateRequest(ChatClearRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  @Override
  protected void handleAuthorized(ChatClearRequest request, HttpServerResponse response) {
    _chat.clearChat(request)
        .onSuccess(event -> handleSuccess(response))
        .onFailure(t -> handleFailure(response, t));
  }

  protected void handleFailure(HttpServerResponse response, Throwable t) {
    handleFailure("Chat cannot be cleared.", response, t);
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "Cleared the chat";
    handleSuccess(message, "text/plain", response);
  }
}
