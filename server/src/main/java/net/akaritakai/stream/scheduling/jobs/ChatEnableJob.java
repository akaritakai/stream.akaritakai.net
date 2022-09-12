package net.akaritakai.stream.scheduling.jobs;

import net.akaritakai.stream.chat.ChatManager;
import net.akaritakai.stream.models.chat.commands.ChatEnableRequest;
import net.akaritakai.stream.scheduling.Utils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ChatEnableJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ChatEnableRequest request = ChatEnableRequest.builder()
                .build();
        ChatManager chatManager = Utils.get(context.getScheduler(), ChatManager.KEY);
        try {
            chatManager.enableChat(request).toCompletionStage().toCompletableFuture().join();
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
