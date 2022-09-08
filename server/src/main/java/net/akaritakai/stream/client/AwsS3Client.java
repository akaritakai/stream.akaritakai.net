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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AwsS3Client {
  private static final Logger LOG = LoggerFactory.getLogger(AwsS3Client.class);
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
        if (!promise.tryFail(e)) {
          LOG.error("unexpected exception", e);
        }
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
        if (!promise.tryFail(e)) {
          LOG.error("unexpected exception", e);
        }
      }
    });
    return promise.future();
  }

  protected static StreamEntry makeStreamEntry(String name, StreamMetadata metadata) {
    return StreamEntry.builder()
            .name(name)
            .metadata(StreamMetadata.builder()
                    .name(metadata.getName())
                    .playlist(metadata.getPlaylist())
                    .duration(metadata.getDuration())
                    .live(metadata.isLive())
                    .build())
            .build();
  }

  public Future<List<StreamEntry>> listMetadataNames(Predicate<String> matcher, int limit) {
    Promise<List<StreamEntry>> promise = Promise.promise();
    _vertx.runOnContext(event -> {
      List<StreamEntry> list = new ArrayList<>(limit);
      class Processor {
        ObjectListing objectListing = _client.listObjects(_bucketName, "media/");
        Iterator<S3ObjectSummary> it = objectListing.getObjectSummaries().iterator();

        void processNext() {
          while (it.hasNext()) {
            S3ObjectSummary summary = it.next();
            int pos = summary.getKey().indexOf('/', 6);
            if (pos > 6 && "/metadata.json".equals(summary.getKey().substring(pos))) {
              String name = summary.getKey().substring(6, pos);
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
                            promise.complete(list);
                          }
                        }
                      })
                      .onFailure(promise::fail);
              return;
            }
          }
          if (objectListing.isTruncated()) {
            objectListing = _client.listNextBatchOfObjects(objectListing);
            it = objectListing.getObjectSummaries().iterator();
            processNext();
            return;
          }
          promise.complete(list);
        }
      }
      new Processor().processNext();
    });
    return promise.future();
  }

  public Future<List<StreamEntry>> listMetadataNames(Predicate<String> matcher) {
    return listMetadataNames(matcher, 10);
  }
}
