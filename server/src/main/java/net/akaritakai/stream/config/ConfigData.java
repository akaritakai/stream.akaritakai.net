package net.akaritakai.stream.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
@JsonDeserialize(builder = ConfigData.ConfigDataBuilder.class)
public class ConfigData {
  boolean development;
  String apiKey;

  // Production mode S3 client settings (where to fetch metadata/media)
  String awsRegion;
  String awsAccessKey;
  String awsSecretKey;
  String awsBucketName;

  // Development mode local file client settings (where to fetch webroot/metadata/media)
  String webRootDir;
  String mediaRootDir;

  // Enable logging request info
  boolean logRequestInfo;

  @JsonPOJOBuilder(withPrefix = "")
  public static class ConfigDataBuilder {
  }
}
