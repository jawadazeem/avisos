/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds CLI configuration from {@code avisos.cli.*} in application.yml. */
@ConfigurationProperties(prefix = "avisos.cli")
public record AvisosCliProperties(boolean enabled) {}
