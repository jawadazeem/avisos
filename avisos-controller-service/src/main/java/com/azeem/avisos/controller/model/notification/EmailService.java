/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.notification;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;
  private final boolean dryRun;
  private final String fromAddress;

  public EmailService(
      ObjectProvider<JavaMailSender> mailSender,
      @Value("${avisos.notification.email.dry-run:true}") boolean dryRun,
      @Value("${avisos.notification.email.from:no-reply@avisos.local}") String fromAddress) {
    this.mailSender = mailSender.getIfAvailable();
    this.dryRun = dryRun;
    this.fromAddress = fromAddress;
  }

  public EmailDeliveryResult send(EmailMessage message) {
    validate(message);

    if (dryRun || mailSender == null) {
      LOGGER.info(
          "Email dry-run: to={} cc={} subject={}", message.to(), message.cc(), message.subject());
      return new EmailDeliveryResult(true, true, "Email logged in dry-run mode");
    }

    SimpleMailMessage mail = new SimpleMailMessage();
    mail.setFrom(fromAddress);
    mail.setTo(message.to());
    if (!message.cc().isEmpty()) {
      mail.setCc(message.cc().toArray(String[]::new));
    }
    mail.setSubject(message.subject());
    mail.setText(message.body());

    mailSender.send(mail);
    return new EmailDeliveryResult(true, false, "Email submitted to mail sender");
  }

  private void validate(EmailMessage message) {
    Objects.requireNonNull(message, "message must not be null");
    if (message.to() == null || message.to().isBlank()) {
      throw new IllegalArgumentException("Email recipient is required");
    }
    if (message.subject() == null || message.subject().isBlank()) {
      throw new IllegalArgumentException("Email subject is required");
    }
    if (message.body() == null || message.body().isBlank()) {
      throw new IllegalArgumentException("Email body is required");
    }
  }
}
