/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.storage;

import com.azeem.avisos.controller.config.AvisosAwsProperties;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Stores flagged camera frames in S3 (LocalStack in dev). Each image is keyed by the originating
 * MQTT source and timestamped for audit trail.
 */
@Service
public class ImageStorageService {
  private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);
  private static final DateTimeFormatter TS_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss-SSS'Z'").withZone(ZoneOffset.UTC);

  private final S3Client s3Client;
  private final String bucketName;
  private boolean bucketVerified = false;

  public ImageStorageService(S3Client s3Client, AvisosAwsProperties props) {
    this.s3Client = s3Client;
    this.bucketName = props.s3BucketName();
  }

  /**
   * Uploads a flagged camera frame to S3.
   *
   * @param source the MQTT source topic (used as key prefix)
   * @param nodeId the originating node UUID
   * @param imageData the raw image bytes
   * @param timestamp the capture timestamp
   * @return the S3 object key for storage in the alarm record
   */
  public String store(String source, UUID nodeId, byte[] imageData, Instant timestamp) {
    ensureBucketExists();

    String sanitizedSource = source.replace("/", "-");
    String objectName = TS_FORMAT.format(timestamp) + ".jpg";
    String s3Key = sanitizedSource + "/" + nodeId + "/" + objectName;

    s3Client.putObject(
        PutObjectRequest.builder().bucket(bucketName).key(s3Key).contentType("image/jpeg").build(),
        RequestBody.fromBytes(imageData));

    log.info("Flagged image stored → s3://{}/{} ({}B)", bucketName, s3Key, imageData.length);
    return s3Key;
  }

  public StoredImage load(String s3Key) {
    ensureBucketExists();

    ResponseBytes<GetObjectResponse> object =
        s3Client.getObjectAsBytes(GetObjectRequest.builder().bucket(bucketName).key(s3Key).build());

    String contentType = object.response().contentType();
    return new StoredImage(
        object.asByteArray(),
        contentType == null || contentType.isBlank() ? "image/jpeg" : contentType);
  }

  private void ensureBucketExists() {
    if (bucketVerified) {
      return;
    }
    try {
      s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
    } catch (NoSuchBucketException e) {
      log.info("Bucket '{}' not found — creating", bucketName);
      s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    }
    bucketVerified = true;
  }

  public record StoredImage(byte[] bytes, String contentType) {}
}
