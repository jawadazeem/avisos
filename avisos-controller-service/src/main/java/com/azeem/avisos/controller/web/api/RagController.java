/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import com.azeem.avisos.controller.service.ai.rag.MarkdownLoaderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for loading markdown files into the RAG system. */
@RestController
@RequestMapping("/api/rag")
public class RagController {
  private final MarkdownLoaderService mdLoaderService;

  public RagController(MarkdownLoaderService mdLoaderService) {
    this.mdLoaderService = mdLoaderService;
  }

  @PostMapping("load")
  public ResponseEntity<Void> load() {
    mdLoaderService.loadKnowledgeFiles();
    return ResponseEntity.ok().build();
  }
}
