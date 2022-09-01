package net.akaritakai.stream.client;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.*;
import net.akaritakai.stream.config.ConfigData;
import net.akaritakai.stream.models.stream.StreamEntry;


public class FakeAwsS3Client extends AwsS3Client {
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
        promise.fail(e);
      }
    });
    return promise.future();
  }

  public Future<List<StreamEntry>> listMetadataNames(Predicate<String> matcher) {
    Promise<List<StreamEntry>> promise = Promise.promise();
    _vertx.runOnContext(event -> {
      try (Stream<Path> stream = Files.list(_mediaRootPath)){
        List<Future<StreamEntry>> futureList = stream
                .map(Path::toFile)
                .filter(File::isDirectory)
                .map(File::getName)
                .filter(name -> !name.startsWith("."))
                .filter(matcher)
                .map(name -> {
                  Promise<StreamEntry> p = Promise.promise();
                  getMetadata(name)
                          .onSuccess(meta -> p.complete(StreamEntry.builder()
                                  .name(name)
                                  .metadataName(meta.getName())
                                  .metadataLive(meta.isLive())
                                  .build()))
                          .onFailure(p::fail);
                  return p.future();
                })
                .collect(Collectors.toList());
        if (futureList.isEmpty()) {
          promise.complete(Collections.emptyList());
        } else {
          Iterator<Future<StreamEntry>> it = futureList.iterator();
          it.next().onComplete(new Handler<AsyncResult<StreamEntry>>() {
            List<StreamEntry> list = new ArrayList<>();
            @Override
            public void handle(AsyncResult<StreamEntry> event) {
              try {
                if (event.succeeded()) {
                  list.add(event.result());
                } else {
                  //
                }
                if (it.hasNext()) {
                  it.next().onComplete(this);
                } else {
                  promise.complete(list);
                }
              } catch (Exception e) {
                promise.fail(e);
              }
            }
          });
        }
      } catch (Exception e) {
        promise.fail(e);
      }
    });
    return promise.future();
  }
}
