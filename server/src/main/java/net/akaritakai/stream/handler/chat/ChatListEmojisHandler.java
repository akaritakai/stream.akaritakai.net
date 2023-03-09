package net.akaritakai.stream.handler.chat;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.chat.ChatManagerMBean;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.chat.ChatEmojiEntry;
import net.akaritakai.stream.models.chat.Entry;
import net.akaritakai.stream.models.chat.request.ChatListEmojisRequest;
import net.akaritakai.stream.models.chat.response.ChatListEmojisResponse;
import net.akaritakai.stream.scheduling.Utils;
import org.apache.commons.lang3.Validate;

import javax.management.ObjectName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Handles the "POST /chat/clear" command.
 */
public class ChatListEmojisHandler extends AbstractHandler<ChatListEmojisRequest> {

  private final ChatManagerMBean _chat;

  public ChatListEmojisHandler(ObjectName chat, CheckAuth checkAuth) {
    super(ChatListEmojisRequest.class, checkAuth);
    _chat = Utils.beanProxy(chat, ChatManagerMBean.class);
  }
  @Override
  protected void validateRequest(ChatListEmojisRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  @Override
  protected void handleAuthorized(HttpServerRequest httpRequest, ChatListEmojisRequest request, HttpServerResponse response) {
    try {
      List<String> entries = _chat.listEmojis(request.getFilter(), 10);

      List<ChatEmojiEntry> emojiEntries = new ArrayList<>(10);
      entries.stream().map(entry -> (Map.Entry<String, String>) Json.decodeValue(entry, Entry.class))
              .forEach(entry -> emojiEntries.add(ChatEmojiEntry.builder()
              .name(entry.getKey())
              .url(toURL(entry.getValue()))
              .build()));

      ChatListEmojisResponse listResponse = ChatListEmojisResponse.builder()
              .entries(emojiEntries)
              .build();

      String responseAsString = OBJECT_MAPPER.writeValueAsString(listResponse);

      handleSuccess(responseAsString, "application/json", response);

    } catch (Throwable t) {
      handleFailure(response, t);
    }
  }
  private static URL toURL(String url) {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  protected void handleFailure(HttpServerResponse response, Throwable t) {
    handleFailure("Emojis cannot be enumerated.", response, t);
  }
}
