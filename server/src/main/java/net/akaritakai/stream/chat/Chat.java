package net.akaritakai.stream.chat;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.config.ConfigData;
import net.akaritakai.stream.handler.chat.*;
import net.akaritakai.stream.scheduling.Utils;
import org.quartz.Scheduler;

public class Chat {
    private final Vertx vertx;
    private final Router router;
    private final ChatManager chatManager;
    private final CheckAuth checkAuth;

    public Chat(Vertx vertx, Router router, Scheduler scheduler, CheckAuth checkAuth) {
        this.vertx = vertx;
        this.router = router;
        this.chatManager = new ChatManager(vertx, scheduler);
        this.checkAuth = checkAuth;
        Utils.set(scheduler, ChatManager.KEY, chatManager);
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
        router.get("/chat")
                .handler(new ChatClientHandler(vertx, chatManager));
    }
}
