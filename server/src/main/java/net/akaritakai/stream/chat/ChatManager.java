package net.akaritakai.stream.chat;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import net.akaritakai.stream.exception.ChatStateConflictException;
import net.akaritakai.stream.models.chat.ChatMessage;
import net.akaritakai.stream.models.chat.ChatSequence;
import net.akaritakai.stream.models.chat.commands.ChatClearRequest;
import net.akaritakai.stream.models.chat.commands.ChatDisableRequest;
import net.akaritakai.stream.models.chat.commands.ChatEnableRequest;
import net.akaritakai.stream.models.chat.request.ChatJoinRequest;
import net.akaritakai.stream.models.chat.request.ChatSendRequest;
import net.akaritakai.stream.models.chat.response.ChatMessageResponse;
import net.akaritakai.stream.models.chat.response.ChatStatusResponse;
import net.akaritakai.stream.scheduling.SchedulerAttribute;
import net.akaritakai.stream.scheduling.Utils;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChatManager {
  public static final SchedulerAttribute<ChatManager> KEY = SchedulerAttribute.instanceOf("chatManager", ChatManager.class);
  private static final Logger LOG = LoggerFactory.getLogger(ChatManager.class);

  private final Vertx _vertx;
  private final AtomicReference<ChatHistory> _history = new AtomicReference<>(null);
  private final Set<ChatListener> _listeners = ConcurrentHashMap.newKeySet();

  private final Scheduler _scheduler;

  public ChatManager(Vertx vertx, Scheduler scheduler) {
    _vertx = vertx;
    _scheduler = scheduler;
  }

  public void sendMessage(ChatSendRequest request) throws ChatStateConflictException {
    LOG.debug("Got ChatSendRequest = {}", request);
    ChatHistory currentHistory = _history.get();
    if (currentHistory == null) {
      throw new ChatStateConflictException("Chat is disabled");
    } else {
      ChatMessage message = currentHistory.addMessage(request);
      ChatMessageResponse response = ChatMessageResponse.builder().message(message).build();
      _listeners.forEach(listener -> _vertx.runOnContext(event -> {
        if (Objects.equals(currentHistory, _history.get())) {
          listener.onMessage(response);
        }
      }));
      JobDataMap jobDataMap = new JobDataMap();
      jobDataMap.put("type", String.valueOf(request.getMessageType()));
      jobDataMap.put("nick", request.getNickname());
      jobDataMap.put("message", request.getMessage());
      Utils.triggerIfExists(_scheduler, "Message", "Chat", jobDataMap);
    }
  }

  public Future<Void> disableChat(ChatDisableRequest request) {
    LOG.info("Got ChatDisableRequest");
    assert request != null;
    Promise<Void> promise = Promise.promise();
    _vertx.runOnContext(disableChatEvent -> {
      ChatHistory currentHistory = _history.get();
      if (currentHistory == null) {
        promise.fail(new ChatStateConflictException("Chat is already disabled"));
      } else {
        _history.set(null);
        ChatStatusResponse response = ChatStatusResponse.builder().enabled(false).build();
        _listeners.forEach(listener -> _vertx.runOnContext(event -> {
          if (_history.get() == null) {
            listener.onStatus(response);
          }
        }));
        promise.complete();
        Utils.triggerIfExists(_scheduler, "disableChat", "Chat");
      }
    });
    return promise.future();
  }

  public Future<Void> enableChat(ChatEnableRequest request) {
    LOG.info("Got ChatEnableRequest");
    assert request != null;
    Promise<Void> promise = Promise.promise();
    _vertx.runOnContext(disableChatEvent -> {
      ChatHistory currentHistory = _history.get();
      if (currentHistory != null) {
        promise.fail(new ChatStateConflictException("Chat is already enabled"));
      } else {
        ChatHistory newHistory = new ChatHistory();
        _history.set(newHistory);
        ChatStatusResponse response = ChatStatusResponse.builder()
            .enabled(true)
            .sequence(ChatSequence.builder()
                .epoch(newHistory.getEpoch())
                .position(0)
                .build())
            .messages(Collections.emptyList())
            .build();
        _listeners.forEach(listener -> _vertx.runOnContext(event -> {
          if (Objects.equals(newHistory, _history.get()))
            listener.onStatus(response);
        }));
        promise.complete();
        Utils.triggerIfExists(_scheduler, "enableChat", "Chat");
      }
    });
    return promise.future();
  }

  public Future<Void> clearChat(ChatClearRequest request) {
    LOG.info("Got ChatClearRequest");
    assert request != null;
    Promise<Void> promise = Promise.promise();
    _vertx.runOnContext(disableChatEvent -> {
      ChatHistory currentHistory = _history.get();
      if (currentHistory == null) {
        promise.fail(new ChatStateConflictException("Chat is disabled"));
      } else {
        ChatHistory newHistory = new ChatHistory();
        _history.set(newHistory);
        ChatStatusResponse response = ChatStatusResponse.builder()
            .enabled(true)
            .sequence(ChatSequence.builder()
                .epoch(newHistory.getEpoch())
                .position(0)
                .build())
            .messages(Collections.emptyList())
            .build();
        _listeners.forEach(listener -> _vertx.runOnContext(event -> {
          if (Objects.equals(newHistory, _history.get()))
            listener.onStatus(response);
        }));
        promise.complete();
        Utils.triggerIfExists(_scheduler, "clearChat", "Chat");
      }
    });
    return promise.future();
  }


  public ChatStatusResponse joinChat(ChatJoinRequest request) {
    LOG.info("Got ChatJoinRequest = {}", request);
    ChatHistory history = _history.get();
    if (history == null) {
      return ChatStatusResponse.builder()
          .enabled(false)
          .build();
    } else {
      Instant requestEpoch = Optional.ofNullable(request)
          .map(ChatJoinRequest::getSequence)
          .map(ChatSequence::getEpoch)
          .orElse(Instant.EPOCH);
      long requestPosition = Optional.ofNullable(request)
          .map(ChatJoinRequest::getSequence)
          .map(ChatSequence::getPosition)
          .orElse(0L);

      List<ChatMessage> messages = history.fetchMessages(requestEpoch, requestPosition);
      long position = 0;
      if (!messages.isEmpty()) {
        ChatMessage lastMessage = messages.get(messages.size() - 1);
        position = lastMessage.getSequence().getPosition();
      }

      return ChatStatusResponse.builder()
          .enabled(true)
          .sequence(ChatSequence.builder()
              .epoch(history.getEpoch())
              .position(position)
              .build())
          .messages(messages)
          .build();
    }
  }

  public void addListener(ChatListener listener) {
    _listeners.add(listener);
  }
}
