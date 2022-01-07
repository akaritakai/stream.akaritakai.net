package net.akaritakai.stream;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import net.akaritakai.stream.chat.ChatManager;
import net.akaritakai.stream.config.Config;
import net.akaritakai.stream.config.ConfigData;
import net.akaritakai.stream.handler.HealthCheckHandler;
import net.akaritakai.stream.handler.TimeHandler;
import net.akaritakai.stream.handler.chat.ChatClearHandler;
import net.akaritakai.stream.handler.chat.ChatClientHandler;
import net.akaritakai.stream.handler.chat.ChatDisableHandler;
import net.akaritakai.stream.handler.chat.ChatEnableHandler;
import net.akaritakai.stream.handler.chat.ChatWriteHandler;
import net.akaritakai.stream.handler.stream.PauseCommandHandler;
import net.akaritakai.stream.handler.stream.ResumeCommandHandler;
import net.akaritakai.stream.handler.stream.StartCommandHandler;
import net.akaritakai.stream.handler.stream.StopCommandHandler;
import net.akaritakai.stream.handler.stream.StreamStatusHandler;
import net.akaritakai.stream.handler.telemetry.TelemetryFetchHandler;
import net.akaritakai.stream.handler.telemetry.TelemetrySendHandler;
import net.akaritakai.stream.streamer.Streamer;
import net.akaritakai.stream.telemetry.TelemetryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    ConfigData config = Config.getConfig();
    LOG.info("Loaded config {}", config);

    Vertx vertx = Vertx.vertx();
    Router router = Router.router(vertx);
    Streamer streamer = new Streamer(vertx, config);
    ChatManager chatManager = new ChatManager(vertx);
    TelemetryStore telemetryStore = new TelemetryStore();

    if (config.isLogRequestInfo()) {
      router.route().handler(event -> {
        HttpServerRequest request = event.request();
        LOG.info("Got request: method={}, uri={}, headers={}",
            request.method(),
            request.absoluteURI(),
            request.headers());
        event.next();
      });
    }

    router.get("/stream/status")
        .handler(new StreamStatusHandler(vertx, streamer));

    router.post("/stream/start")
        .handler(BodyHandler.create())
        .handler(new StartCommandHandler(streamer, config.getApiKey()));
    router.post("/stream/stop")
        .handler(BodyHandler.create())
        .handler(new StopCommandHandler(streamer, config.getApiKey()));
    router.post("/stream/pause")
        .handler(BodyHandler.create())
        .handler(new PauseCommandHandler(streamer, config.getApiKey()));
    router.post("/stream/resume")
        .handler(BodyHandler.create())
        .handler(new ResumeCommandHandler(streamer, config.getApiKey()));

    router.post("/chat/clear")
        .handler(BodyHandler.create())
        .handler(new ChatClearHandler(chatManager, config.getApiKey()));
    router.post("/chat/disable")
        .handler(BodyHandler.create())
        .handler(new ChatDisableHandler(chatManager, config.getApiKey()));
    router.post("/chat/enable")
        .handler(BodyHandler.create())
        .handler(new ChatEnableHandler(chatManager, config.getApiKey()));
    router.post("/chat/write")
        .handler(BodyHandler.create())
        .handler(new ChatWriteHandler(chatManager, config.getApiKey()));
    router.get("/chat")
        .handler(new ChatClientHandler(vertx, chatManager));

    router.post("/telemetry/fetch")
        .handler(BodyHandler.create())
        .handler(new TelemetryFetchHandler(telemetryStore, config.getApiKey()));
    router.get("/telemetry")
        .handler(new TelemetrySendHandler(telemetryStore));

    router.get("/health").handler(new HealthCheckHandler());
    router.get("/time").handler(new TimeHandler());

    if (config.isDevelopment()) {
      router.route().handler(event -> {
        event.response().putHeader("Access-Control-Allow-Origin", "*");
        event.next();
      });
      router.route("/media/*").handler(StaticHandler.create()
          .setAllowRootFileSystemAccess(true)
          .setCachingEnabled(false)
          .setAlwaysAsyncFS(true)
          .setEnableFSTuning(true)
          .setEnableRangeSupport(true)
          .setWebRoot(config.getMediaRootDir()));
      router.route().handler(StaticHandler.create()
          .setAllowRootFileSystemAccess(true)
          .setCachingEnabled(false)
          .setAlwaysAsyncFS(true)
          .setEnableFSTuning(true)
          .setEnableRangeSupport(true)
          .setWebRoot(config.getWebRootDir()));
    }

    vertx.createHttpServer()
        .requestHandler(router)
        .listen(config.getPort(), event -> {
          if (event.succeeded()) {
            LOG.info("Started the server on port {}", event.result().actualPort());
          } else {
            LOG.error("Failed to start up the server", event.cause());
          }
        });
  }
}
