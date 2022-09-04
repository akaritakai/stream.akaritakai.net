package net.akaritakai.stream.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
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

  public Future<List<StreamEntry>> listMetadataNames(Predicate<String> matcher, int limit) {
    Promise<List<StreamEntry>> promise = Promise.promise();
    try {
      _vertx.runOnContext(event -> {
        Stream<Path> stream;
        try {
          stream = Files.list(_mediaRootPath);
        } catch (IOException e) {
          promise.fail(e);
          return;
        }
        List<StreamEntry> list = new ArrayList<>(limit);
        class Processor {
          Iterator<Path> it = stream.iterator();

          void complete() {
            promise.complete(list);
            stream.close();
          }

          void fail(Throwable e) {
            promise.fail(e);
            stream.close();
          }

          void processNext() {
            while (it.hasNext()) {
              File f = it.next().toFile();
              if (!f.isDirectory()) {
                continue;
              }
              String name = f.getName();
              if (name.startsWith(".")) {
                continue;
              }
              boolean nameMatch = matcher.test(name);
              getMetadata(name)
                      .onSuccess(metadata -> {
                        if (!nameMatch && !matcher.test(metadata.getName())) {
                          processNext();
                        } else {
                          list.add(makeStreamEntry(name, metadata));
                          if (list.size() < limit) {
                            processNext();
                          } else {
                            complete();
                          }
                        }
                      })
                      .onFailure(this::fail);
              return;
            }
            complete();
          }
        }
        new Processor().processNext();
      });
    } catch (Exception e) {
      promise.fail(e);
    }
    return promise.future();
  }
}
