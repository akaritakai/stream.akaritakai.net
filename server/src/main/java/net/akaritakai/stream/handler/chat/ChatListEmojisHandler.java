package net.akaritakai.stream.handler.chat;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.chat.ChatManager;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.chat.ChatEmojiEntry;
import net.akaritakai.stream.models.chat.request.ChatListEmojisRequest;
import net.akaritakai.stream.models.chat.response.ChatListEmojisResponse;
import org.apache.commons.lang3.Validate;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;


/**
 * Handles the "POST /chat/clear" command.
 */
public class ChatListEmojisHandler extends AbstractHandler<ChatListEmojisRequest> {

  private final ChatManager _chat;

  public ChatListEmojisHandler(ChatManager chat, CheckAuth checkAuth) {
    super(ChatListEmojisRequest.class, checkAuth);
    _chat = chat;
  }
  @Override
  protected void validateRequest(ChatListEmojisRequest request) {
    Validate.notNull(request, "request cannot be null");
    Validate.notEmpty(request.getKey(), "key cannot be null/empty");
  }

  @Override
  protected void handleAuthorized(HttpServerRequest httpRequest, ChatListEmojisRequest request, HttpServerResponse response) {
    try {
      List<Map.Entry<String, URL>> entries = _chat.listEmojis(request.getFilter() != null && !request.getFilter().isBlank() ? new Predicate<>() {
        final Pattern pattern = Pattern.compile(request.getFilter().trim(), Pattern.CASE_INSENSITIVE);

        @Override
        public boolean test(String s) {
          return pattern.matcher(s).find();
        }
      } : any -> true, 10);

      List<ChatEmojiEntry> emojiEntries = new ArrayList<>(10);
      entries.forEach(entry -> emojiEntries.add(ChatEmojiEntry.builder()
              .name(entry.getKey())
              .url(entry.getValue())
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

  protected void handleFailure(HttpServerResponse response, Throwable t) {
    handleFailure("Emojis cannot be enumerated.", response, t);
  }
}
