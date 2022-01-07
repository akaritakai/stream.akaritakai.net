package net.akaritakai.stream.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import net.akaritakai.stream.config.ConfigData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FakeAwsS3Client extends AwsS3Client {
  private static final Logger LOG = LoggerFactory.getLogger(FakeAwsS3Client.class);
  private final Path _mediaRootPath;

  public FakeAwsS3Client(Vertx vertx, ConfigData config) {
    super(vertx, config);
    _mediaRootPath = Paths.get(config.getMediaRootDir());
  }

  @Override
  protected String getMetadataFilePath(String name) {
    return String.format("%s/metadata.json", name);
  }

  @Override
  protected Future<String> getFileData(String path) {
    Promise<String> promise = Promise.promise();
    _vertx.runOnContext(event -> {
      try {
        String result = Files.readString(_mediaRootPath.resolve(path));
        promise.complete(result);
      } catch (Exception e) {
        if (!promise.tryFail(e)) {
          LOG.error("unexpected exception", e);
        }
      }
    });
    return promise.future();
  }
}
