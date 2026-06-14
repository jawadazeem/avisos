/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import com.azeem.avisos.controller.exceptions.CriticalInfrastructureException;
import com.azeem.avisos.controller.security.model.SecurityContext;
import com.azeem.avisos.controller.service.threat.KeywordThreatDetector;
import com.azeem.avisos.controller.service.threat.ThreatDetector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Central configuration class that enables all {@code @ConfigurationProperties}, provides bridge
 * beans from Spring properties to domain config records, and creates beans that cannot be
 * auto-scanned (records with runtime constructor args, singletons, shared ObjectMapper).
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties({
  AvisosMqttProperties.class,
  AvisosVisionProperties.class,
  AvisosDatabaseProperties.class,
  AvisosNodeServiceProperties.class,
  AvisosCliProperties.class,
  AvisosAwsProperties.class,
  RagProperties.class
})
public class ServiceConfiguration {

  /** Bridge: Spring properties -> domain config record used by MQTT components. */
  @Bean
  public MqttConfig mqttConfig(AvisosMqttProperties props) {
    return new MqttConfig(
        props.controllerClientId(),
        props.broker(),
        props.topic(),
        props.connectionTimeout(),
        props.cleanSession(),
        props.automaticReconnect());
  }

  /** Bridge: Spring properties -> domain config record used by vision components. */
  @Bean
  public VisionConfig visionConfig(AvisosVisionProperties props) {
    return new VisionConfig(props.apiUrl(), props.minConfidence(), props.timeoutSeconds());
  }

  /** Bridge: Spring properties -> domain config record used by node service. */
  @Bean
  public NodeServiceConfig nodeServiceConfig(AvisosNodeServiceProperties props) {
    return new NodeServiceConfig(props.staleThreshold(), props.minHeartbeatIntervalMs());
  }

  /** Shared Jackson ObjectMapper with Java time and parameter names support. */
  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new ParameterNamesModule());
  }

  /** Loads threat labels from {@code problematic-labels.yml} and creates the detector. */
  @Bean
  public ThreatDetector threatDetector() {
    ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());
    ymlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
    try (InputStream is = getClass().getResourceAsStream("/problematic-labels.yml")) {
      if (is == null) {
        throw new CriticalInfrastructureException(
            "CRITICAL: problematic-labels.yml not found in classpath!");
      }
      LabelConfig config = ymlMapper.readValue(is, LabelConfig.class);
      return new KeywordThreatDetector(config.critical(), config.warning());
    } catch (IOException e) {
      throw new CriticalInfrastructureException("Failed to load problematic labels: " + e);
    }
  }

  /** Thread-local security context -- must be a singleton. */
  @Bean
  public SecurityContext securityContext() {
    return new SecurityContext();
  }
}
