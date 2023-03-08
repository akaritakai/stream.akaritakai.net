package net.akaritakai.stream.exception;

import java.util.concurrent.CompletionException;

public class StreamStateConflictException extends CompletionException {

  public StreamStateConflictException(String message) {
    super(message);
  }

}
