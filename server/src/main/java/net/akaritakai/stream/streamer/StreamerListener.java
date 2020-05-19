package net.akaritakai.stream.streamer;

import net.akaritakai.stream.models.stream.StreamState;


public interface StreamerListener {
  void onStateUpdate(StreamState state);
}
