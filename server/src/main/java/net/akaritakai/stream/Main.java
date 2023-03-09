package net.akaritakai.stream;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import net.akaritakai.stream.chat.Chat;
import net.akaritakai.stream.config.Config;
import net.akaritakai.stream.config.ConfigData;
import net.akaritakai.stream.debug.TouchTimer;
import net.akaritakai.stream.handler.HealthCheckHandler;
import net.akaritakai.stream.handler.RedirectHandler;
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
import net.akaritakai.stream.streamer.StreamerMBean;
import net.akaritakai.stream.telemetry.TelemetryStore;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.nio.channels.ClosedChannelException;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    TouchTimer startTimer = new TouchTimer();
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
    argumentParser.addArgument("--sslPort")
            .dest("sslPort")
            .help("Service TCP port number")
            .type(Integer.class)
            .setDefault(8443)
            .metavar("PORT");
    argumentParser.addArgument("--apiKey")
            .dest("apiKey")
            .help("API Configuration Key")
            .metavar("KEY");
    argumentParser.addArgument("--sslApi")
            .dest("sslApi")
            .type(Boolean.class)
            .help("API Configuration requires SSL");
      argumentParser.addArgument("--sslMedia")
              .dest("sslMedia")
              .type(Boolean.class)
              .setDefault(false)
              .help("Media available on SSL");
    argumentParser.addArgument("--emojisFile")
            .dest("emojisFile")
            .help("Custom emojis json file")
            .metavar("FILE");
    Namespace ns;
    try {
      ns = argumentParser.parseArgs(args);
    } catch (ArgumentParserException e) {
      argumentParser.handleError(e);
      System.exit(1);
      return;
    }

    boolean sslApi = Optional.ofNullable(ns.getBoolean("sslApi")).orElse(false);
    boolean sslMedia = Optional.ofNullable(ns.getBoolean("sslMedia")).orElse(false);

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
    startTimer.touch("Loaded config");

    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    Optional<SelfSignedCertificate> ssc = Optional.of(SelfSignedCertificate.create());
    ssc.ifPresent(s -> Runtime.getRuntime().addShutdownHook(new Thread(s::delete)));



    ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByMimeType("application/javascript");
    LOG.info("ScriptEngine: {}", scriptEngine);
    startTimer.touch("ScriptEngine: {}", scriptEngine);

    Scheduler scheduler = SetupScheduler.createFactory().getScheduler();
    Utils.set(scheduler, Config.KEY, config);
    startTimer.touch("scheduler created");

    CheckAuth auth = new CheckAuthImpl(
            Optional.ofNullable(ns.getString("apiKey")).orElse(config.getApiKey()));

    ObjectName streamerName = new ObjectName("net.akaritakai.stream:type=Streamer");
    ObjectName chatManagerName = new ObjectName("net.akaritakai.stream:type=ChatManager");

    Vertx vertx = Vertx.vertx().exceptionHandler(ex -> {
      LOG.error("Exception", ex);
    });
    Router router = Router.router(vertx);
    Router sslRouter = Router.router(vertx);
    Optional.of((Handler<RoutingContext>) event -> {
        TouchTimer.of(event).ifPresent(touchTimer -> touchTimer.touch("failureHandler"));
        if (event.failure() instanceof ClosedChannelException) {
            LOG.debug("Closed Channel, {}", TouchTimer.of(event).orElse(null));
        } else if (!event.response().headWritten()) {
            event.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end(Json.encode(event.failure()));
        } else {
            LOG.error("Unhandled exception in router, {}", TouchTimer.of(event).orElse(null), event.failure());
        }
    }).ifPresent(handler -> {
        Handler<RoutingContext> ctx = event -> {
            event.data().put("TouchTimer", new TouchTimer().touch("uri={}", event.request().uri()));
            event.next();
        };
        router.route().handler(ctx).failureHandler(handler);
        sslRouter.route().handler(ctx).failureHandler(handler);
    });

    Streamer streamer = new Streamer(vertx, config, scheduler);
    mBeanServer.registerMBean(streamer, streamerName);
    Utils.set(scheduler, StreamerMBean.KEY, streamerName);
    startTimer.touch("streamer created");

    TelemetryStore telemetryStore = new TelemetryStore();

    if (config.isLogRequestInfo()) {
      Optional.of((Handler<RoutingContext>) event -> {
        HttpServerRequest request = event.request();
        LOG.info("Got request: method={}, uri={}, headers={}",
            request.method(),
            request.absoluteURI(),
            request.headers());
        event.next();
      }).ifPresent(handler -> {
        router.route().handler(handler);
        sslRouter.route().handler(handler);
      });
    }

    Optional.of(new StreamStatusHandler(vertx, streamerName))
            .ifPresent(handler -> {
              router.get("/stream/status").handler(handler);
              sslRouter.get("/stream/status").handler(handler);
            });

    Optional.of(new StartCommandHandler(streamerName, auth, vertx))
            .ifPresent(handler -> {
              if (!sslApi) {
                router.post("/stream/start").handler(BodyHandler.create()).handler(handler);
              }
              sslRouter.post("/stream/start").handler(BodyHandler.create()).handler(handler);
            });

    Optional.of(new StopCommandHandler(streamerName, auth))
            .ifPresent(handler -> {
              if (!sslApi) {
                router.post("/stream/stop").handler(BodyHandler.create()).handler(handler);
              }
              sslRouter.post("/stream/stop").handler(BodyHandler.create()).handler(handler);
            });

    Optional.of(new PauseCommandHandler(streamerName, auth, vertx))
            .ifPresent(handler -> {
              if (!sslApi) {
                router.post("/stream/pause").handler(BodyHandler.create()).handler(handler);
              }
              sslRouter.post("/stream/pause").handler(BodyHandler.create()).handler(handler);
            });

    Optional.of(new ResumeCommandHandler(streamerName, auth, vertx))
            .ifPresent(handler -> {
              if (!sslApi) {
                router.post("/stream/resume").handler(BodyHandler.create()).handler(handler);
              }
              sslRouter.post("/stream/resume").handler(BodyHandler.create()).handler(handler);
            });

    Optional.of(new DirCommandHandler(streamerName, auth, vertx))
            .ifPresent(handler -> {
              if (!sslApi) {
                router.post("/stream/dir").handler(BodyHandler.create()).handler(handler);
              }
              sslRouter.post("/stream/dir").handler(BodyHandler.create()).handler(handler);
            });

    new Chat(vertx, router, sslRouter, scheduler, auth, sslApi, mBeanServer, chatManagerName)
            .addCustomEmojis(Optional.ofNullable(ns.getString("emojisFile")).map(File::new)
                    .orElse(Optional.ofNullable(config.getEmojisFile()).map(File::new).orElse(null)))
            .install();

    Optional.of(new TelemetryFetchHandler(telemetryStore, auth))
            .ifPresent(handler -> {
              if (!sslApi) {
                router.post("/telemetry/fetch").handler(BodyHandler.create()).handler(handler);
              }
              sslRouter.post("/telemetry/fetch").handler(BodyHandler.create()).handler(handler);
            });

    Optional.of(new TelemetrySendHandler(telemetryStore))
            .ifPresent(handler -> {
              router.get("/telemetry").handler(handler);
              sslRouter.get("/telemetry").handler(handler);
            });

    Optional.of(new LogFetchHandler(vertx, auth))
            .ifPresent(handler -> {
              if (!sslApi) {
                router.post("/log/fetch").handler(BodyHandler.create()).handler(handler);
              }
              sslRouter.post("/log/fetch").handler(BodyHandler.create()).handler(handler);
            });

    Optional.of(new LogSendHandler())
            .ifPresent(handler -> {
              router.get("/log").handler(handler);
              sslRouter.get("/log").handler(handler);
            });

    Optional.of(new JobsHandler(scheduler, auth))
            .ifPresent(handler -> {
              if (!sslApi) {
                router.post("/quartz/jobs").handler(BodyHandler.create()).handler(handler);
              }
              sslRouter.post("/quartz/jobs").handler(BodyHandler.create()).handler(handler);
            });

    Optional.of(new TriggersHandler(scheduler, auth))
            .ifPresent(handler -> {
              if (!sslApi) {
                router.post("/quartz/triggers").handler(BodyHandler.create()).handler(handler);
              }
              sslRouter.post("/quartz/triggers").handler(BodyHandler.create()).handler(handler);
            });

    Optional.of(new HealthCheckHandler())
            .ifPresent(handler -> {
              router.get("/health").handler(handler);
              sslRouter.get("/health").handler(handler);
            });

    Optional.of(new TimeHandler())
            .ifPresent(handler -> {
              router.get("/time").handler(handler);
              sslRouter.get("/time").handler(handler);
            });

      final int[] httpPort = new int[1];

      if (config.isDevelopment()) {
      Optional.of((Handler<RoutingContext>) event -> {
        event.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        event.next();
      }).ifPresent(handler -> {
        router.route().handler(handler);
        sslRouter.route().handler(handler);
      });

      Optional.of(StaticHandler.create(FileSystemAccess.ROOT, config.getMediaRootDir())
          .setCachingEnabled(true)
          .setCacheEntryTimeout(TimeUnit.SECONDS.toMillis(10))
          .setMaxAgeSeconds(2) // let us use 2 second segments
          .setMaxCacheSize(32 * 1024 * 1024)
          .setDirectoryListing(false)
          .setIncludeHidden(false)
          .setAlwaysAsyncFS(true)
          .setEnableFSTuning(true)
          .setEnableRangeSupport(true)).ifPresent(handler -> {
        router.route("/media/*").handler(handler);
        LOG.info("sslMedia = {}", sslMedia);
        sslRouter.route("/media/*").handler(sslMedia ? handler : new RedirectHandler(() -> httpPort[0]));
      });

      Optional.of(StaticHandler.create(/*FileSystemAccess.ROOT, config.getWebRootDir()*/)
          .setCachingEnabled(true)
          .setCacheEntryTimeout(TimeUnit.HOURS.toMillis(2))
          .setMaxAgeSeconds(TimeUnit.HOURS.toSeconds(2))
          .setMaxCacheSize(32 * 1024 * 1024)
          .setSendVaryHeader(true)
          .setDirectoryListing(false)
          .setIncludeHidden(false)
          .skipCompressionForSuffixes(new HashSet<>(Arrays.asList("jpg", "png", "woff2")))
          .setAlwaysAsyncFS(true)
          .setFilesReadOnly(true)
          .setEnableFSTuning(true)
          .setEnableRangeSupport(true)).ifPresent(handler -> {
        router.route().handler(handler);
        sslRouter.route().handler(handler);
      });
    }

    startTimer.touch("SetupScheduler");
    SetupScheduler.setup(scheduler);

    Promise<Void> startHttp = Promise.promise();
    Promise<Void> startHttps = Promise.promise();

    HttpServerOptions httpServerOptions = new HttpServerOptions();
    HttpServerOptions httpsServerOptions = new HttpServerOptions();

    ssc.ifPresent(selfSignedCertificate -> {
      httpsServerOptions.setSsl(true);
      httpsServerOptions.setPemKeyCertOptions(selfSignedCertificate.keyCertOptions());
      httpsServerOptions.setTrustOptions(selfSignedCertificate.trustOptions());
    });

    startTimer.touch("createHttpServer");
    vertx.createHttpServer(httpServerOptions)
            .requestHandler(router)
            .listen(Optional.ofNullable(ns.getInt("port")).orElse(config.getPort()), event -> {
              if (event.succeeded()) {
                startTimer.touch("http:listen done, port {}", event.result().actualPort());
                LOG.info("Started the http server on port {}", event.result().actualPort());
                httpPort[0] = event.result().actualPort();
                startHttp.complete();
              } else {
                startTimer.touch("http:listen failed, {}", event.cause().getMessage());
                LOG.error("http:listen failed", event.cause());
                startHttp.fail(event.cause());
              }
            });

    startTimer.touch("createHttpsServer");
    vertx.createHttpServer(httpsServerOptions)
            .requestHandler(sslRouter)
            .listen(Optional.ofNullable(ns.getInt("sslPort")).orElse(0), event -> {
              if (event.succeeded()) {
                startTimer.touch("https:listen done, port {}", event.result().actualPort());
                LOG.info("Started the https server on port {}", event.result().actualPort());
                startHttps.complete();
              } else {
                startTimer.touch("https:listen failed, {}", event.cause().getMessage());
                LOG.error("https:listen failed", event.cause());
                startHttps.fail(event.cause());
              }
            });

    CompositeFuture.all(startHttp.future(), startHttps.future())
            .onSuccess(event -> {
              startTimer.touch("Ports bound");
              try {
                scheduler.startDelayed(1);
                Utils.triggerIfExists(scheduler, "start");
              } catch (SchedulerException e) {
                LOG.error("Failed to start scheduler", e);
                shutdown(vertx);
                throw new RuntimeException(e);
              }
              LOG.info("Startup completed {}", startTimer);
            })
            .onFailure(ex -> {
              LOG.error("Failed to start up the server: {}", startTimer, ex);
              startHttp.fail(ex);
              shutdown(vertx);
              throw new RuntimeException(ex);
            });

  }
  private static void shutdown(Vertx vertx) {
    vertx.close().onComplete(ev -> System.exit(-1));
  }
}
