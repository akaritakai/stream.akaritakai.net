package net.akaritakai.stream.client;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.*;
import net.akaritakai.stream.config.ConfigData;
import net.akaritakai.stream.models.stream.StreamEntry;
import net.akaritakai.stream.models.stream.StreamMetadata;
import org.apache.commons.io.IOUtils;


public class AwsS3Client {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  protected final Vertx _vertx;
  private final AmazonS3 _client;
  private final String _bucketName;

  public AwsS3Client(Vertx vertx, ConfigData config) {
    _vertx = vertx;
    _client = buildAwsClient(config);
    _bucketName = config.getAwsBucketName();
  }

  private static AmazonS3 buildAwsClient(ConfigData config) {
    if (config.isDevelopment()) {
      return null; // We won't build the S3 client for dev usage
    }
    AWSCredentials credentials = new BasicAWSCredentials(config.getAwsAccessKey(), config.getAwsSecretKey());
    AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
    return AmazonS3ClientBuilder
        .standard()
        .withRegion(config.getAwsRegion())
        .withCredentials(credentialsProvider)
        .build();
  }

  public Future<StreamMetadata> getMetadata(String name) {
    Promise<StreamMetadata> promise = Promise.promise();
    getFileData(getMetadataFilePath(name)).onFailure(promise::fail).onSuccess(data -> {
      try {
        StreamMetadata result = OBJECT_MAPPER.readValue(data, StreamMetadata.class);
        promise.complete(result);
      } catch (Exception e) {
        promise.fail(e);
      }
    });
    return promise.future();
  }

  protected String getMetadataFilePath(String name) {
    return String.format("media/%s/metadata.json", name);
  }

  protected Future<String> getFileData(String path) {
    Promise<String> promise = Promise.promise();
    _vertx.runOnContext(event -> {
      try (S3Object object = _client.getObject(_bucketName, path);
          S3ObjectInputStream is = object.getObjectContent()) {
        String result = IOUtils.toString(is, StandardCharsets.UTF_8);
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
      try {
        List<Future<StreamEntry>> list = new ArrayList<>();
        ObjectListing objectListing = _client.listObjects(_bucketName, "media/");
        for (;;) {
          for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
            int pos = summary.getKey().indexOf('/', 6);
            if (pos > 6 && "/metadata.json".equals(summary.getKey().substring(pos))) {
              String name = summary.getKey().substring(6, pos);
              boolean nameMatch = matcher.test(name);
              Promise<StreamEntry> res = Promise.promise();
              getMetadata(name)
                      .onSuccess(metadata -> {
                        if (!nameMatch && !matcher.test(metadata.getName())) {
                          res.complete(null);
                        } else {
                          res.complete(StreamEntry.builder()
                                  .name(name)
                                  .metadata(StreamMetadata.builder()
                                          .name(metadata.getName())
                                          .playlist(metadata.getPlaylist())
                                          .duration(metadata.getDuration())
                                          .live(metadata.isLive())
                                          .build())
                                  .build());
                        }
                      })
                      .onFailure(res::fail);
              list.add(res.future());
            }
          }

          if (objectListing.isTruncated()) {
            objectListing = _client.listNextBatchOfObjects(objectListing);
          } else {
            if (list.isEmpty()) {
              promise.complete(Collections.emptyList());
            } else {
              Iterator<Future<StreamEntry>> it = list.iterator();
              it.next().onComplete(new Handler<>() {
                List<StreamEntry> strings = new ArrayList<>();
                @Override
                public void handle(AsyncResult<StreamEntry> event) {
                  try {
                    if (event.succeeded() && event.result() != null) {
                      strings.add(event.result());
                    }
                    if (it.hasNext()) {
                      it.next().onComplete(this);
                    } else {
                      promise.complete(strings);
                    }
                  } catch (Exception e) {
                    promise.fail(e);
                  }
                }
              });
            }
          }
        }
      } catch (Exception e) {
        promise.fail(e);
      }
    });
    return promise.future();
  }
}
