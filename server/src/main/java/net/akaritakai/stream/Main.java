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
import net.akaritakai.stream.handler.info.LogFetchHandler;
import net.akaritakai.stream.handler.info.LogSendHandler;
import net.akaritakai.stream.handler.quartz.JobsHandler;
import net.akaritakai.stream.handler.quartz.TriggersHandler;
import net.akaritakai.stream.handler.stream.DirCommandHandler;
import net.akaritakai.stream.handler.stream.PauseCommandHandler;
import net.akaritakai.stream.handler.stream.ResumeCommandHandler;
import net.akaritakai.stream.handler.stream.StartCommandHandler;
import net.akaritakai.stream.handler.stream.StopCommandHandler;
import net.akaritakai.stream.handler.stream.StreamStatusHandler;
import net.akaritakai.stream.handler.telemetry.TelemetryFetchHandler;
import net.akaritakai.stream.handler.telemetry.TelemetrySendHandler;
import net.akaritakai.stream.scheduling.SetupScheduler;
import net.akaritakai.stream.scheduling.Utils;
import net.akaritakai.stream.streamer.Streamer;
import net.akaritakai.stream.telemetry.TelemetryStore;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.JDBC;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;


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
    argumentParser.addArgument("--apiKey")
            .dest("apiKey")
            .help("API Configuration Key")
            .metavar("KEY");
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

    ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByMimeType("application/javascript");
    LOG.info("ScriptEngine: {}", scriptEngine);

    StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
    Properties schedulerProperties = new Properties();
    schedulerProperties.setProperty("org.quartz.threadPool.class", org.quartz.simpl.SimpleThreadPool.class.getName());
    schedulerProperties.setProperty("org.quartz.threadPool.threadCount", "4");
    schedulerProperties.setProperty("org.quartz.jobStore.class", org.quartz.impl.jdbcjobstore.JobStoreTX.class.getName());
    schedulerProperties.setProperty("org.quartz.jobStore.driverDelegateClass", org.quartz.impl.jdbcjobstore.StdJDBCDelegate.class.getName());
    schedulerProperties.setProperty("org.quartz.jobStore.dataSource", "myDS");
    schedulerProperties.setProperty("org.quartz.jobStore.useProperties", "true");
    schedulerProperties.setProperty("org.quartz.dataSource.myDS.driver", JDBC.class.getName());
    schedulerProperties.setProperty("org.quartz.dataSource.myDS.URL", "jdbc:sqlite:scheduler.db");

    try (Connection connection = DriverManager.getConnection(schedulerProperties.getProperty("org.quartz.dataSource.myDS.URL"));
         ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM QRTZ_LOCKS")) {
      LOG.info("Database already initialized");
    } catch (SQLException ex) {
      LOG.warn("Attempting to reinitialize the database");
      InitDB.main(new String[0]);
    }

    schedulerFactory.initialize(schedulerProperties);
    Scheduler scheduler = schedulerFactory.getScheduler();
    Utils.set(scheduler, Config.KEY, config);

    CheckAuth auth = new CheckAuthImpl(
            Optional.ofNullable(ns.getString("apiKey")).orElse(config.getApiKey()));

    Vertx vertx = Vertx.vertx();
    Router router = Router.router(vertx);
    Streamer streamer = new Streamer(vertx, config, scheduler);
    TelemetryStore telemetryStore = new TelemetryStore();

    Utils.set(scheduler, Streamer.KEY, streamer);

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
        .handler(new StartCommandHandler(streamer, auth));
    router.post("/stream/stop")
        .handler(BodyHandler.create())
        .handler(new StopCommandHandler(streamer, auth));
    router.post("/stream/pause")
        .handler(BodyHandler.create())
        .handler(new PauseCommandHandler(streamer, auth));
    router.post("/stream/resume")
        .handler(BodyHandler.create())
        .handler(new ResumeCommandHandler(streamer, auth));
    router.post("/stream/dir")
        .handler(BodyHandler.create())
        .handler(new DirCommandHandler(streamer, auth));

    new Chat(vertx, router, scheduler, auth).install();

    router.post("/telemetry/fetch")
        .handler(BodyHandler.create())
        .handler(new TelemetryFetchHandler(telemetryStore, auth));
    router.get("/telemetry")
        .handler(new TelemetrySendHandler(telemetryStore));

    router.post("/log/fetch")
            .handler(BodyHandler.create())
            .handler(new LogFetchHandler(vertx, auth));
    router.get("/log")
            .handler(new LogSendHandler());

    router.post("/quartz/jobs")
            .handler(BodyHandler.create())
            .handler(new JobsHandler(scheduler, auth));
    router.post("/quartz/triggers")
            .handler(BodyHandler.create())
            .handler(new TriggersHandler(scheduler, auth));

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

    SetupScheduler.setup(scheduler);

    scheduler.start();

    vertx.createHttpServer()
        .requestHandler(router)
        .listen(Optional.ofNullable(ns.getInt("port")).orElse(config.getPort()), event -> {
          if (event.succeeded()) {
            LOG.info("Started the server on port {}", event.result().actualPort());
            Utils.triggerIfExists(scheduler, "start");
          } else {
            LOG.error("Failed to start up the server", event.cause());
          }
        });
  }
}
