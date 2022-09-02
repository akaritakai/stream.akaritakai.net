package net.akaritakai.stream;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import net.akaritakai.stream.chat.Chat;
import net.akaritakai.stream.config.Config;
import net.akaritakai.stream.config.ConfigData;
import net.akaritakai.stream.handler.HealthCheckHandler;
import net.akaritakai.stream.handler.TimeHandler;
import net.akaritakai.stream.handler.stream.DirCommandHandler;
import net.akaritakai.stream.handler.stream.PauseCommandHandler;
import net.akaritakai.stream.handler.stream.ResumeCommandHandler;
import net.akaritakai.stream.handler.stream.StartCommandHandler;
import net.akaritakai.stream.handler.stream.StopCommandHandler;
import net.akaritakai.stream.handler.stream.StreamStatusHandler;
import net.akaritakai.stream.handler.telemetry.TelemetryFetchHandler;
import net.akaritakai.stream.handler.telemetry.TelemetrySendHandler;
import net.akaritakai.stream.scheduling.Utils;
import net.akaritakai.stream.streamer.Streamer;
import net.akaritakai.stream.telemetry.TelemetryStore;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;


public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    ArgumentParser argumentParser = ArgumentParsers.newArgumentParser("StreamServer")
            .defaultHelp(true)
            .description("A basic video sharing and streaming service");
    argumentParser.addArgument("-f", "--configFile")
            .dest("configFile")
            .help("Configuration file")
            .metavar("FILE");
    argumentParser.addArgument("-p", "--port")
            .dest("port")
            .help("Service TCP port number")
            .type(Integer.class)
            .metavar("PORT");
    Namespace ns;
    try {
      ns = argumentParser.parseArgs(args);
    } catch (ArgumentParserException e) {
      argumentParser.handleError(e);
      System.exit(1);
      return;
    }

    ConfigData config = Config.getConfig(Optional
                    .ofNullable(ns.getString("configFile"))
                    .map(File::new)
                    .filter(file -> {
                      if (file.isFile() && file.canRead()) {
                        return true;
                      } else {
                        LOG.error("File not found or not readable: {}", file);
                        return false;
                      }
                    }).map(file -> {
                      try {
                        return file.toURI().toURL();
                      } catch (MalformedURLException e) {
                        LOG.error("Unexpected exception", e);
                        throw new RuntimeException(e);
                      }
                    }).orElse(null));

    LOG.info("Loaded config {}", config);

    StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
    Scheduler scheduler = schedulerFactory.getScheduler();

    Vertx vertx = Vertx.vertx();
    Router router = Router.router(vertx);
    Streamer streamer = new Streamer(vertx, config, scheduler);
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
    router.post("/stream/dir")
        .handler(BodyHandler.create())
        .handler(new DirCommandHandler(streamer, config.getApiKey()));

    new Chat(config, vertx, router, scheduler).install();

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
      router.route("/media/*").handler(StaticHandler.create(FileSystemAccess.ROOT, config.getMediaRootDir())
          .setCachingEnabled(false)
          .setAlwaysAsyncFS(true)
          .setEnableFSTuning(true)
          .setEnableRangeSupport(true));
      router.route().handler(StaticHandler.create(/*FileSystemAccess.ROOT, config.getWebRootDir()*/)
          .setCachingEnabled(false)
          .setAlwaysAsyncFS(true)
          .setEnableFSTuning(true)
          .setEnableRangeSupport(true));
    }

    vertx.createHttpServer()
        .requestHandler(router)
        .listen(Optional.ofNullable(ns.getInt("port")).orElse(config.getPort()), event -> {
          if (event.succeeded()) {
            LOG.info("Started the server on port {}", event.result().actualPort());
            try {
              scheduler.start();
            } catch (SchedulerException e) {
              LOG.error("Error starting scheduler", e);
              System.exit(2);
            }
            Utils.triggerIfExists(scheduler, "start");
          } else {
            LOG.error("Failed to start up the server", event.cause());
          }
        });
  }
}
