package net.akaritakai.stream.scheduling.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akaritakai.stream.chat.ChatManagerMBean;
import net.akaritakai.stream.handler.Util;
import net.akaritakai.stream.models.chat.ChatMessageType;
import net.akaritakai.stream.models.chat.request.ChatSendRequest;
import net.akaritakai.stream.scheduling.Utils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.management.ObjectName;

public class ChatSendJob implements Job {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ChatSendRequest request = ChatSendRequest.builder()
                .messageType(ChatMessageType.valueOf(context.get(ChatMessageType.class.getSimpleName()).toString()))
                .nickname(context.get("nickname").toString())
                .message(context.get("message").toString())
                .build();
        ObjectName objectName = Utils.get(context.getScheduler(), ChatManagerMBean.KEY);
        ChatManagerMBean chatManager = Utils.beanProxy(objectName, ChatManagerMBean.class);
        try {
            chatManager.sendMessage(objectMapper.writeValueAsString(request), Util.ANY);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
