/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.ai.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class SubjectMatterExpertChatService {
  private final ChatClient chatClient;

  public SubjectMatterExpertChatService(
      ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
    this.chatClient =
        chatClientBuilder.defaultAdvisors(new QuestionAnswerAdvisor(vectorStore)).build();
  }

  public String ask(String query) {
    return chatClient.prompt().user(query).call().content();
  }
}
