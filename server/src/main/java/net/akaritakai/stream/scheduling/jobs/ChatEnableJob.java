package net.akaritakai.stream.scheduling.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akaritakai.stream.chat.ChatManagerMBean;
import net.akaritakai.stream.models.chat.commands.ChatEnableRequest;
import net.akaritakai.stream.scheduling.Utils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.management.ObjectName;

public class ChatEnableJob implements Job {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ChatEnableRequest request = ChatEnableRequest.builder()
                .build();
        ObjectName objectName = Utils.get(context.getScheduler(), ChatManagerMBean.KEY);
        ChatManagerMBean chatManager = Utils.beanProxy(objectName, ChatManagerMBean.class);
        try {
            chatManager.enableChat(objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
