/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds database configuration from {@code avisos.database.*} in application.yml. */
@ConfigurationProperties(prefix = "avisos.database")
public record AvisosDatabaseProperties(String url) {}
