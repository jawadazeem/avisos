/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.ai.rag;

import com.azeem.avisos.controller.config.RagProperties;
import com.azeem.avisos.controller.exceptions.CannotFindKnowledgeFileException;
import com.azeem.avisos.controller.exceptions.CannotLoadKnowledgeFilesExceptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class MarkdownLoaderService {
  private static final Logger log = LoggerFactory.getLogger(MarkdownLoaderService.class);
  private final VectorStore vectorStore;
  private final RagProperties ragProperties;

  public MarkdownLoaderService(VectorStore vectorStore, RagProperties ragProperties) {
    this.vectorStore = vectorStore;
    this.ragProperties = ragProperties;
  }

  public void loadKnowledgeFiles() {
    Path knowledgeBaseDir = ragProperties.knowledgeBaseDir();
    if (knowledgeBaseDir == null || !Files.isDirectory(knowledgeBaseDir)) {
      throw new CannotFindKnowledgeFileException(
          "Knowledge base directory does not exist or is not a directory: " + knowledgeBaseDir);
    }

    List<Path> markdownFiles = new ArrayList<>();

    try (Stream<Path> paths = Files.walk(knowledgeBaseDir)) {
      paths
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith(".md"))
          .sorted(Comparator.comparing(Path::toString))
          .forEach(markdownFiles::add);
    } catch (IOException e) {
      throw new CannotLoadKnowledgeFilesExceptions(
          "Cannot read knowledge files from directory: " + knowledgeBaseDir, e);
    }

    if (markdownFiles.isEmpty()) {
      throw new CannotFindKnowledgeFileException(
          "No Markdown files found in knowledge base directory: " + knowledgeBaseDir);
    }

    MarkdownDocumentReaderConfig readerConfig = MarkdownDocumentReaderConfig.defaultConfig();
    List<Document> documents = new ArrayList<>();
    for (Path markdownFile : markdownFiles) {
      Resource resource = new FileSystemResource(markdownFile);
      documents.addAll(new MarkdownDocumentReader(resource, readerConfig).get());
    }

    this.vectorStore.add(documents);

    log.info(
        "Successfully loaded {} documents from {} markdown files into the RAG system.",
        documents.size(),
        markdownFiles.size());
  }
}
