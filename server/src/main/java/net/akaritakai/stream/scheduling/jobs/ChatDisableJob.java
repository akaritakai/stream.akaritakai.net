package net.akaritakai.stream.scheduling.jobs;

import net.akaritakai.stream.chat.ChatManagerMBean;
import net.akaritakai.stream.models.chat.commands.ChatDisableRequest;
import org.quartz.JobExecutionContext;

public class ChatDisableJob extends AbstractChatJob {
    @Override
    protected void execute1(JobExecutionContext context, ChatManagerMBean chatManager) throws Exception {
        ChatDisableRequest request = ChatDisableRequest.builder()
                .build();
        chatManager.disableChat(writeValueAsString(request));
    }
}
