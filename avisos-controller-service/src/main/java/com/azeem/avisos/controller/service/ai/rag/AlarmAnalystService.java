/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.ai.rag;

import com.azeem.avisos.controller.exceptions.ResourceNotFoundException;
import com.azeem.avisos.controller.model.alarm.AlarmAnalysisRecord;
import com.azeem.avisos.controller.repository.AlarmAnalysisRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class AlarmAnalystService {
  private static final Logger log = LoggerFactory.getLogger(MarkdownLoaderService.class);

  private final ChatClient chatClient;
  private final VectorStore vectorStore;
  private final AlarmAnalysisRepository alertAnalysisRepository;

  public AlarmAnalystService(
      ChatClient.Builder builder,
      VectorStore vectorStore,
      AlarmAnalysisRepository alertAnalysisRepository) {
    this.vectorStore = vectorStore;
    this.alertAnalysisRepository = alertAnalysisRepository;
    this.chatClient =
        builder
            .defaultSystem(
                """
                        You are a Critical Incident Response Agent.
                        Your output is a structured Incident Brief for security personnel.

                        Format:
                        ALERT: [Type of Incident]
                        LOCATION: [Detailed Location from context]
                        STATUS: [Active/Degraded/Critical]
                        EVIDENCE: [Retrieve specific sensor data/Rekognition labels]
                        CONTEXTUAL HISTORY: [Reference 1-2 key facts from the node history]
                        IMMEDIATE ACTION: [Specific directive based on AI Analyst notes]

                        RULES:
                        - If Rekognition labels indicate "Water" or "Pool", treat as high confidence.
                        - If historical data says 'AI analyst note: mandatory immediate escalation',
                          include 'PRIORITY: URGENT' at the top.
                        - NO fluff. NO polite conversational filler.
                        """)
            .build();
  }

  public void saveAnalysis(AlarmAnalysisRecord alarmAnalysisRecord) {
    alertAnalysisRepository.saveAnalysis(alarmAnalysisRecord);
  }

  public AlarmAnalysisRecord getAnalysis(UUID uuid) {
    return alertAnalysisRepository
        .findByAlarmId(uuid.toString())
        .orElseThrow(
            () -> {
              log.warn("No analysis found for alarm ID: {}", uuid);
              return new ResourceNotFoundException("Analysis record not found for: " + uuid);
            });
  }

  public String generateAlert(String nodeId, String rawAlarmData, String rekognitionLabels) {
    return chatClient
        .prompt()
        .user("Alarm: " + rawAlarmData + ". Visual evidence: " + rekognitionLabels)
        .call()
        .content();
  }
}
