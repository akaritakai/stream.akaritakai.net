package net.akaritakai.stream.handler.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.chat.ChatManagerMBean;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.chat.commands.ChatClearRequest;
import net.akaritakai.stream.scheduling.Utils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


/**
 * Handles the "POST /chat/clear" command.
 */
public class ChatClearHandler extends AbstractHandler<ChatClearRequest> {
  private static final Logger LOG = LoggerFactory.getLogger(ChatClearHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Vertx _vertx;
  private final ObjectName _chat;

  public ChatClearHandler(ObjectName chat, CheckAuth checkAuth, Vertx vertx) {
    super(ChatClearRequest.class, checkAuth);
    _chat = chat;
    _vertx = vertx;
  }
  @Override
  protected void validateRequest(ChatClearRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  @Override
  protected void handleAuthorized(HttpServerRequest httpRequest, ChatClearRequest request, HttpServerResponse response) {
    CompletableFuture
            .runAsync(() -> {
              try {
                Utils.beanProxy(_chat, ChatManagerMBean.class).clearChat(OBJECT_MAPPER.writeValueAsString(request));
              } catch (JsonProcessingException e) {
                throw new CompletionException(e);
              }
            })
            .whenComplete(((unused, ex) -> {
              _vertx.runOnContext(event -> {
                if (ex == null) {
                  handleSuccess(response);
                } else {
                  handleFailure(response, ex);
                }
              });
            }));
  }

  protected void handleFailure(HttpServerResponse response, Throwable t) {
    handleFailure("Chat cannot be cleared.", response, t);
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "Cleared the chat";
    handleSuccess(message, "text/plain", response);
  }
}
