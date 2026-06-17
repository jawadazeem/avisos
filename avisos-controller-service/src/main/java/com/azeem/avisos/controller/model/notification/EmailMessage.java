/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.notification;

import java.util.List;

public record EmailMessage(String to, List<String> cc, String subject, String body) {

  public EmailMessage {
    cc = cc == null ? List.of() : List.copyOf(cc);
  }
}
