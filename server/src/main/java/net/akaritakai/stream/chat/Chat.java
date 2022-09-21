package net.akaritakai.stream.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.config.ConfigData;
import net.akaritakai.stream.handler.chat.*;
import net.akaritakai.stream.scheduling.Utils;
import org.quartz.Scheduler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class Chat {
    private final Vertx vertx;
    private final Router router;
    private final ChatManager chatManager;
    private final CheckAuth checkAuth;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Chat(Vertx vertx, Router router, Scheduler scheduler, CheckAuth checkAuth) {
        this.vertx = vertx;
        this.router = router;
        this.chatManager = new ChatManager(vertx, scheduler);
        this.checkAuth = checkAuth;
        Utils.set(scheduler, ChatManager.KEY, chatManager);
    }

    public Chat addCustomEmojis(File jsonFile) throws IOException {
        if (jsonFile != null) {
            for (Map.Entry<String, String> entry : objectMapper
                    .readValue(jsonFile, new TypeReference<Map<String, String>>() {
                    }).entrySet()) {
                if (entry.getValue() != null) {
                    if (entry.getKey().isBlank()) {
                        throw new IllegalArgumentException("key name can't be blank");
                    }
                    chatManager.setCustomEmoji(":" + entry.getKey() + ":", new URL(entry.getValue()));
                }
            }
        }
        return this;
    }

    public void install() {
        router.post("/chat/clear")
                .handler(BodyHandler.create())
                .handler(new ChatClearHandler(chatManager, checkAuth));
        router.post("/chat/disable")
                .handler(BodyHandler.create())
                .handler(new ChatDisableHandler(chatManager, checkAuth));
        router.post("/chat/enable")
                .handler(BodyHandler.create())
                .handler(new ChatEnableHandler(chatManager, checkAuth));
        router.post("/chat/write")
                .handler(BodyHandler.create())
                .handler(new ChatWriteHandler(chatManager, checkAuth));
        router.post("/chat/emojis")
                .handler(BodyHandler.create())
                .handler(new ChatListEmojisHandler(chatManager, checkAuth));
        router.post("/chat/emoji")
                .handler(BodyHandler.create())
                .handler(new ChatSetEmojiHandler(chatManager, checkAuth));
        router.get("/chat")
                .handler(new ChatClientHandler(vertx, chatManager));
    }
}
