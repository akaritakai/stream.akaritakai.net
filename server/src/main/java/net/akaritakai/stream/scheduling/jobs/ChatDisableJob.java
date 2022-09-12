package net.akaritakai.stream.scheduling.jobs;

import net.akaritakai.stream.chat.ChatManager;
import net.akaritakai.stream.models.chat.commands.ChatDisableRequest;
import net.akaritakai.stream.scheduling.Utils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ChatDisableJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ChatDisableRequest request = ChatDisableRequest.builder()
                .build();
        ChatManager chatManager = Utils.get(context.getScheduler(), ChatManager.KEY);
        try {
            chatManager.disableChat(request).toCompletionStage().toCompletableFuture().join();
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
