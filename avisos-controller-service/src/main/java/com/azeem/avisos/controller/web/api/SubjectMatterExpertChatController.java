/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import com.azeem.avisos.controller.service.ai.rag.SubjectMatterExpertChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/ask-sme")
public class SubjectMatterExpertChatController {
  private final SubjectMatterExpertChatService smeService;

  public SubjectMatterExpertChatController(SubjectMatterExpertChatService smeService) {
    this.smeService = smeService;
  }

  @GetMapping("/chat")
  public String chat(@RequestParam("query") String query) {
    return smeService.ask(query);
  }
}
