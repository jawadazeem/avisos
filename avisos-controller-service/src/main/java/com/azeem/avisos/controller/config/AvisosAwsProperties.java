/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds AWS/LocalStack configuration from {@code avisos.aws.*} in application.yml. */
@ConfigurationProperties(prefix = "avisos.aws")
public record AvisosAwsProperties(
    String endpoint,
    String region,
    String s3BucketName,
    String accessKeyId,
    String secretAccessKey) {}
