package net.akaritakai.stream.scheduling.jobs;

import net.akaritakai.stream.chat.ChatManagerMBean;
import net.akaritakai.stream.scheduling.Utils;
import org.quartz.JobExecutionContext;

import javax.management.ObjectName;

public abstract class AbstractChatJob extends AbstractJob {
    @Override
    protected void execute0(JobExecutionContext context) throws Exception {
        ObjectName objectName = Utils.get(context.getScheduler(), ChatManagerMBean.KEY);
        ChatManagerMBean chatManager = Utils.beanProxy(objectName, ChatManagerMBean.class);
        execute1(context, chatManager);
    }

    protected abstract void execute1(JobExecutionContext context, ChatManagerMBean chatManager) throws Exception;
}
