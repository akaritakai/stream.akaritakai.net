package net.akaritakai.stream.client;

import java.nio.charset.StandardCharsets;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import net.akaritakai.stream.config.ConfigData;
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
}
