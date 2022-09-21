package net.akaritakai.stream.handler.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.chat.ChatManager;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.chat.commands.ChatEnableRequest;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the "POST /chat/enable" command.
 */
public class ChatEnableHandler extends AbstractHandler<ChatEnableRequest> {
  private static final Logger LOG = LoggerFactory.getLogger(ChatEnableHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final ChatManager _chat;

  public ChatEnableHandler(ChatManager chat, CheckAuth checkAuth) {
    super(ChatEnableRequest.class, checkAuth);
    _chat = chat;
  }
  @Override
  protected void validateRequest(ChatEnableRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  protected void handleAuthorized(HttpServerRequest httpRequest, ChatEnableRequest request, HttpServerResponse response) {
    _chat.enableChat(request)
        .onSuccess(event -> handleSuccess(response))
        .onFailure(t -> handleFailure(response, t));
  }

  protected void handleFailure(HttpServerResponse response, Throwable t) {
    handleFailure("Chat cannot be enabled.", response, t);
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "Enabled the chat";
    handleSuccess(message, "text/plain", response);
  }
}
