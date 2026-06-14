/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/** Spring Boot entry point for the Avisos Controller Service. */
@SpringBootApplication(scanBasePackages = "com.azeem.avisos.controller")
@EnableConfigurationProperties
public class AvisosControllerServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(AvisosControllerServiceApplication.class, args);
  }
}
