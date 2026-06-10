/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS infrastructure beans. Points at LocalStack in dev/test; in production, swap the endpoint and
 * credentials for real AWS.
 */
@Configuration
public class AwsConfiguration {
  private static final Logger log = LoggerFactory.getLogger(AwsConfiguration.class);

  @Bean
  public S3Client s3Client(AvisosAwsProperties props) {
    log.info("Initializing S3 client → endpoint={}, region={}", props.endpoint(), props.region());
    return S3Client.builder()
        .endpointOverride(URI.create(props.endpoint()))
        .region(Region.of(props.region()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.accessKeyId(), props.secretAccessKey())))
        .forcePathStyle(true)
        .build();
  }
}