package net.akaritakai.stream.chat;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import net.akaritakai.stream.config.ConfigData;
import net.akaritakai.stream.handler.chat.ChatClearHandler;
import net.akaritakai.stream.handler.chat.ChatClientHandler;
import net.akaritakai.stream.handler.chat.ChatDisableHandler;
import net.akaritakai.stream.handler.chat.ChatEnableHandler;

public class Chat {
    private final ConfigData config;
    private final Vertx vertx;
    private final Router router;
    private final ChatManager chatManager;

    public Chat(ConfigData config, Vertx vertx, Router router) {
        this.config = config;
        this.vertx = vertx;
        this.router = router;
        this.chatManager = new ChatManager(vertx);
    }

    public void install() {
        router.post("/chat/clear")
                .handler(BodyHandler.create())
                .handler(new ChatClearHandler(chatManager, config.getApiKey()));
        router.post("/chat/disable")
                .handler(BodyHandler.create())
                .handler(new ChatDisableHandler(chatManager, config.getApiKey()));
        router.post("/chat/enable")
                .handler(BodyHandler.create())
                .handler(new ChatEnableHandler(chatManager, config.getApiKey()));
        router.get("/chat")
                .handler(new ChatClientHandler(vertx, chatManager));
    }
}
