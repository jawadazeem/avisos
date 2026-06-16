/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import com.azeem.avisos.controller.service.ai.rag.MarkdownLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorDatabaseInitializer {

  private static final Logger log = LoggerFactory.getLogger(VectorDatabaseInitializer.class);

  @Bean
  public CommandLineRunner initVectorDatabase(MarkdownLoaderService loaderService) {
    return args -> {
      log.info("Starting initial vector database load...");
      try {
        loaderService.loadKnowledgeFiles();
        log.info("Vector database initialization complete.");
      } catch (Exception e) {
        log.error("Failed to initialize vector database", e);
      }
    };
  }
}
