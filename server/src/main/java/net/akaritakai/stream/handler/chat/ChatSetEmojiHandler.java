package net.akaritakai.stream.handler.chat;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.chat.ChatManager;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.chat.request.ChatSetEmojisRequest;
import org.apache.commons.lang3.Validate;


/**
 * Handles the "POST /chat/clear" command.
 */
public class ChatSetEmojiHandler extends AbstractHandler<ChatSetEmojisRequest> {

  private final ChatManager _chat;

  public ChatSetEmojiHandler(ChatManager chat, CheckAuth checkAuth) {
    super(ChatSetEmojisRequest.class, checkAuth);
    _chat = chat;
  }
  @Override
  protected void validateRequest(ChatSetEmojisRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
    Validate.notNull(request.getEmoji(), "emoji cannot be null/empty");
    Validate.isTrue(request.getEmoji().getName().startsWith(":"), "name should start with colon");
    Validate.isTrue(request.getEmoji().getName().endsWith(":"), "name should end with colon");
  }

  @Override
  protected void handleAuthorized(HttpServerRequest httpRequest, ChatSetEmojisRequest request, HttpServerResponse response) {
    try {
      _chat.setCustomEmoji(request.getEmoji().getName(), request.getEmoji().getUrl());

      handleSuccess("OK", "text/plain", response);

    } catch (Throwable t) {
      handleFailure(response, t);
    }
  }

  protected void handleFailure(HttpServerResponse response, Throwable t) {
    handleFailure("Emojis cannot be set.", response, t);
  }
}
