package net.akaritakai.stream.scheduling.jobs;

import net.akaritakai.stream.chat.ChatManagerMBean;
import net.akaritakai.stream.models.chat.commands.ChatEnableRequest;
import org.quartz.JobExecutionContext;

public class ChatEnableJob extends AbstractChatJob {
    @Override
    protected void execute1(JobExecutionContext context, ChatManagerMBean chatManager) throws Exception {
        ChatEnableRequest request = ChatEnableRequest.builder()
                .build();
        chatManager.enableChat(writeValueAsString(request));
    }
}
