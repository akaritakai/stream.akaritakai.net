package net.akaritakai.stream.chat;

import net.akaritakai.stream.models.chat.response.ChatMessageResponse;
import net.akaritakai.stream.models.chat.response.ChatStatusResponse;


public interface ChatListener {

  void onStatus(ChatStatusResponse status);
  void onMessage(ChatMessageResponse message);

}
