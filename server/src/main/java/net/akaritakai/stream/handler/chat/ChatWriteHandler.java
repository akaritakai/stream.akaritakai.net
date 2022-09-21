package net.akaritakai.stream.handler.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.chat.ChatManager;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.handler.Util;
import net.akaritakai.stream.models.chat.commands.ChatWriteRequest;
import net.akaritakai.stream.models.chat.request.ChatSendRequest;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the "POST /chat/write" command.
 */
public class ChatWriteHandler extends AbstractHandler<ChatWriteRequest> {
  private static final Logger LOG = LoggerFactory.getLogger(ChatWriteHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final ChatManager _chat;

  public ChatWriteHandler(ChatManager chat, CheckAuth checkAuth) {
    super(ChatWriteRequest.class, checkAuth);
    _chat = chat;
  }
  @Override
  protected void validateRequest(ChatWriteRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  protected void handleAuthorized(HttpServerRequest httpRequest, ChatWriteRequest request, HttpServerResponse response) {
    try {
      _chat.sendMessage(ChatSendRequest.builder()
              .messageType(request.getMessageType())
              .nickname(request.getNickname())
              .message(request.getMessage())
              .build(), Util.getIpAddressFromRequest(httpRequest));
      handleSuccess(response);
    } catch (Exception ex) {
      handleFailure(response, ex);
    }
  }

  protected void handleFailure(HttpServerResponse response, Throwable t) {
    handleFailure("Chat write failed.", response, t);
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "OK";
    handleSuccess(message, "text/plain", response);
  }
}
