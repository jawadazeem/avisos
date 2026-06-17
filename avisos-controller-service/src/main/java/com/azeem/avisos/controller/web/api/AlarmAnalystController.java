/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import com.azeem.avisos.controller.model.alarm.AlarmAnalysisRecord;
import com.azeem.avisos.controller.service.ai.rag.AlarmAnalystService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/analyses")
public class AlarmAnalystController {
  private final AlarmAnalystService alertAnalystService;

  public AlarmAnalystController(AlarmAnalystService alertAnalystService) {
    this.alertAnalystService = alertAnalystService;
  }

  @GetMapping
  public List<AlarmAnalysisRecord> getAllAnalyses() {
    return alertAnalystService.getAllAnalyses();
  }

  @GetMapping("/{id}")
  public AlarmAnalysisRecord getAlarmAnalysis(@PathVariable UUID id) {
    return alertAnalystService.getAnalysis(id);
  }
}
