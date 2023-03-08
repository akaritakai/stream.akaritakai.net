package net.akaritakai.stream.exception;

import java.util.concurrent.CompletionException;

public class ChatStateConflictException extends CompletionException {

  public ChatStateConflictException(String message) {
    super(message);
  }

}
