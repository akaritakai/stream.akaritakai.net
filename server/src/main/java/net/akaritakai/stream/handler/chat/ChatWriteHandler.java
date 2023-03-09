package net.akaritakai.stream.handler.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.chat.ChatManagerMBean;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.handler.Util;
import net.akaritakai.stream.models.chat.commands.ChatWriteRequest;
import net.akaritakai.stream.models.chat.request.ChatSendRequest;
import net.akaritakai.stream.scheduling.Utils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;


/**
 * Handles the "POST /chat/write" command.
 */
public class ChatWriteHandler extends AbstractHandler<ChatWriteRequest> {
  private static final Logger LOG = LoggerFactory.getLogger(ChatWriteHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final ChatManagerMBean _chat;

  public ChatWriteHandler(ObjectName chat, CheckAuth checkAuth) {
    super(ChatWriteRequest.class, checkAuth);
    _chat = Utils.beanProxy(chat, ChatManagerMBean.class);
  }
  @Override
  protected void validateRequest(ChatWriteRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  protected void handleAuthorized(HttpServerRequest httpRequest, ChatWriteRequest request, HttpServerResponse response) {
    try {
      _chat.sendMessage(OBJECT_MAPPER.writeValueAsString(ChatSendRequest.builder()
              .messageType(request.getMessageType())
              .nickname(request.getNickname())
              .message(request.getMessage())
              .source(Util.getIpAddressFromRequest(httpRequest))
              .build()));
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
