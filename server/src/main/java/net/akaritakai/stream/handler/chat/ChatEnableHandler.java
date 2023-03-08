package net.akaritakai.stream.handler.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.chat.ChatManagerMBean;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.chat.commands.ChatEnableRequest;
import net.akaritakai.stream.scheduling.Utils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


/**
 * Handles the "POST /chat/enable" command.
 */
public class ChatEnableHandler extends AbstractHandler<ChatEnableRequest> {
  private static final Logger LOG = LoggerFactory.getLogger(ChatEnableHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Vertx _vertx;
  private final ObjectName _chat;

  public ChatEnableHandler(ObjectName chat, CheckAuth checkAuth, Vertx vertx) {
    super(ChatEnableRequest.class, checkAuth);
    _chat = chat;
    _vertx = vertx;
  }
  @Override
  protected void validateRequest(ChatEnableRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  protected void handleAuthorized(HttpServerRequest httpRequest, ChatEnableRequest request, HttpServerResponse response) {
    CompletableFuture
            .runAsync(() -> {
              try {
                Utils.beanProxy(_chat, ChatManagerMBean.class).enableChat(OBJECT_MAPPER.writeValueAsString(request));
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
    handleFailure("Chat cannot be enabled.", response, t);
  }

  private void handleSuccess(HttpServerResponse response) {
    String message = "Enabled the chat";
    handleSuccess(message, "text/plain", response);
  }
}
