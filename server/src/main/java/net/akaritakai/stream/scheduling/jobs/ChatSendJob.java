package net.akaritakai.stream.scheduling.jobs;

import net.akaritakai.stream.chat.ChatManager;
import net.akaritakai.stream.exception.ChatStateConflictException;
import net.akaritakai.stream.models.chat.ChatMessageType;
import net.akaritakai.stream.models.chat.request.ChatSendRequest;
import net.akaritakai.stream.scheduling.Utils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ChatSendJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ChatSendRequest request = ChatSendRequest.builder()
                .messageType(ChatMessageType.valueOf(context.get(ChatMessageType.class.getSimpleName()).toString()))
                .nickname(context.get("nickname").toString())
                .message(context.get("message").toString())
                .build();
        ChatManager chatManager = Utils.get(context.getScheduler(), ChatManager.KEY);
        try {
            chatManager.sendMessage(request);
        } catch (ChatStateConflictException e) {
            throw new JobExecutionException(e);
        }
    }
}
