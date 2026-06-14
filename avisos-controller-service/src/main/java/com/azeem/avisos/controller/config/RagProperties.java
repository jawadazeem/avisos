/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds RAG knowledge base configuration from {@code avisos.ai.rag.*}. */
@ConfigurationProperties(prefix = "avisos.ai.rag")
public record RagProperties(Path knowledgeBaseDir) {}
