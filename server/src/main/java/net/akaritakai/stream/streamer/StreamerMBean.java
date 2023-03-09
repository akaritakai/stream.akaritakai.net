package net.akaritakai.stream.streamer;

import net.akaritakai.stream.scheduling.SchedulerAttribute;

import javax.management.NotificationEmitter;
import javax.management.ObjectName;
import java.util.List;

public interface StreamerMBean extends NotificationEmitter {
    SchedulerAttribute<ObjectName> KEY = SchedulerAttribute.instanceOf(StreamerMBean.class.getName(), ObjectName.class);

    String getState();
    void stopStream(String streamStopRequest);
    void resumeStream(String streamResumeRequest);
    void pauseStream(String streamPauseRequest);
    void startStream(String streamStartRequest);
    List<String> listStreams(String filter);
}
