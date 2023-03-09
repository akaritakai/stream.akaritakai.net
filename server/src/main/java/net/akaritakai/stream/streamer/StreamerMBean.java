package net.akaritakai.stream.streamer;

import net.akaritakai.stream.models.stream.StreamStateType;
import net.akaritakai.stream.scheduling.SchedulerAttribute;

import javax.management.NotificationEmitter;
import javax.management.ObjectName;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public interface StreamerMBean extends NotificationEmitter {
    SchedulerAttribute<ObjectName> KEY = SchedulerAttribute.instanceOf(StreamerMBean.class.getName(), ObjectName.class);

    String getState();
    void stopStream(String streamStopRequest);
    void resumeStream(String streamResumeRequest);
    void pauseStream(String streamPauseRequest);
    void startStream(String streamStartRequest);
    List<String> listStreams(String filter);
    String getStatus();
    boolean isLive();
    String getCurrentPlaylist();
    String getCurrentMediaName();
    Duration getCurrentDuration();
    Instant getCurrentStartTime();
    Instant getCurrentEndTime();

}
