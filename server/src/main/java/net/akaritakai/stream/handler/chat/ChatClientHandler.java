package net.akaritakai.stream.handler.chat;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.RoutingContext;
import net.akaritakai.stream.chat.ChatListener;
import net.akaritakai.stream.chat.ChatManager;
import net.akaritakai.stream.models.chat.request.ChatJoinRequest;
import net.akaritakai.stream.models.chat.request.ChatPartRequest;
import net.akaritakai.stream.models.chat.request.ChatRequest;
import net.akaritakai.stream.models.chat.request.ChatSendRequest;
import net.akaritakai.stream.models.chat.response.ChatMessageResponse;
import net.akaritakai.stream.models.chat.response.ChatStatusResponse;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles websocket requests to "/chat"
 */
public class ChatClientHandler implements Handler<RoutingContext>, ChatListener {
  private static final Logger LOG = LoggerFactory.getLogger(ChatClientHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Vertx _vertx;
  private final ChatManager _chat;
  private final Set<ServerWebSocket> _sockets = ConcurrentHashMap.newKeySet();

  public ChatClientHandler(Vertx vertx, ChatManager chat) {
    _vertx = vertx;
    _chat = chat;
    _chat.addListener(this);
  }

  @Override
  public void handle(RoutingContext event) {
    ServerWebSocket socket = event.request().upgrade();
    _sockets.add(socket);
    socket.endHandler(endEvent -> _sockets.remove(socket));
    socket.closeHandler(closeEvent -> _sockets.remove(socket));
    socket.textMessageHandler(textMessage -> {
      try {
        ChatRequest request = OBJECT_MAPPER.readValue(textMessage, ChatRequest.class);
        if (request instanceof ChatJoinRequest) {
          try {
            ChatStatusResponse status = _chat.joinChat((ChatJoinRequest) request);
            String response = OBJECT_MAPPER.writeValueAsString(status);
            socket.writeTextMessage(response);
          } catch (Exception e) {
            // This should only occur if there's some sort of serialization error
            LOG.warn("Unable to respond to join request. Reason: {}: {}", e.getClass().getCanonicalName(), e.getMessage());
            socket.close((short) 500, Throwables.getStackTraceAsString(e));
          }
        } else if (request instanceof ChatSendRequest) {
          try {
            validateChatSendRequest((ChatSendRequest) request);
            try {
              _chat.sendMessage((ChatSendRequest) request);
            } catch (Exception e) {
              // This should only occur if the server is not available
              LOG.warn("Unable to send chat message. Reason: {}: {}", e.getClass().getCanonicalName(), e.getMessage());
              socket.close((short) 409, e.getMessage());
            }
          } catch (Exception e) {
            // This should occur if the request is invalid
            LOG.warn("Got invalid chat request: {}", e.getMessage());
            socket.close((short) 400, e.getMessage());
          }
        } else if (request instanceof ChatPartRequest) {
          socket.close((short) 200); // Close handler should take care of this event
        }
      } catch (Exception e) {
        LOG.warn("Unable to deserialize client request into ChatRequest object. Reason: {}: {}",
            e.getClass().getCanonicalName(), e.getMessage());
        socket.close((short) 400, "Bad Request"); // 400: Bad Request
      }
    });
  }

  public void validateChatSendRequest(ChatSendRequest request) {
    Validate.notNull(request.getMessageType(), "messageType cannot be null");
    Validate.notNull(request.getNickname(), "nickname cannot be null");
    Validate.notEmpty(request.getNickname(), "nickname cannot be empty");
    Validate.isTrue(request.getNickname().length() <= 25, "nickname cannot be more than 25 characters");
    Validate.isTrue(!request.getNickname().startsWith("_"), "nickname cannot start with an underscore");
    Validate.isTrue(request.getNickname().matches("^[A-Za-z0-9_]*$"), "nickname must use the character set: [A-Za-z0-9_]");
    Validate.notNull(request.getMessage(), "message cannot be null");
    Validate.notEmpty(request.getMessage(), "message cannot be empty");
    Validate.isTrue(request.getMessage().length() <= 500, "message must not be more than 400 characters");
  }

  @Override
  public void onStatus(ChatStatusResponse status) {
    try {
      String response = OBJECT_MAPPER.writeValueAsString(status);
      _sockets.forEach(socket -> _vertx.runOnContext(event -> socket.writeTextMessage(response)));
    } catch (Exception e) {
      LOG.error("Unable to send status update to clients", e);
    }
  }

  @Override
  public void onMessage(ChatMessageResponse message) {
    try {
      String response = OBJECT_MAPPER.writeValueAsString(message);
      _sockets.forEach(socket -> _vertx.runOnContext(event -> socket.writeTextMessage(response)));
    } catch (Exception e) {
      LOG.error("Unable to send status update to clients", e);
    }
  }
}
