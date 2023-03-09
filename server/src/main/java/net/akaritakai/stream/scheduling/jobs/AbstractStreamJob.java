package net.akaritakai.stream.scheduling.jobs;

import net.akaritakai.stream.chat.ChatManagerMBean;
import net.akaritakai.stream.scheduling.Utils;
import net.akaritakai.stream.streamer.StreamerMBean;
import org.quartz.JobExecutionContext;

import javax.management.ObjectName;

public abstract class AbstractStreamJob extends AbstractJob {
    @Override
    protected void execute0(JobExecutionContext context) throws Exception {
        ObjectName objectName = Utils.get(context.getScheduler(), ChatManagerMBean.KEY);
        StreamerMBean streamer = Utils.beanProxy(objectName, StreamerMBean.class);
        execute1(context, streamer);
    }

    protected abstract void execute1(JobExecutionContext context, StreamerMBean streamer) throws Exception;
}
