package net.akaritakai.stream.scheduling.jobs;

import net.akaritakai.stream.chat.ChatManagerMBean;
import net.akaritakai.stream.handler.Util;
import net.akaritakai.stream.models.chat.ChatMessageType;
import net.akaritakai.stream.models.chat.request.ChatSendRequest;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ChatSendJob extends AbstractChatJob {
    @Override
    protected void execute1(JobExecutionContext context, ChatManagerMBean chatManager) throws JobExecutionException {
        ChatSendRequest request = ChatSendRequest.builder()
                .messageType(ChatMessageType.valueOf(context.get(ChatMessageType.class.getSimpleName()).toString()))
                .nickname(context.get("nickname").toString())
                .message(context.get("message").toString())
                .build();
        chatManager.sendMessage(writeValueAsString(request), Util.ANY);
    }
}
