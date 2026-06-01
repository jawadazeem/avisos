/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.framework;

import com.azeem.avisos.node.config.AppConfig;
import com.azeem.avisos.node.config.MqttConfig;
import com.azeem.avisos.node.config.NodeConfig;
import com.azeem.avisos.node.exception.MissingConfigFileException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

/**
 * Loads and resolves application configuration from YAML with environment variable overrides.
 *
 * <p>This is the node-service adaptation of the DIY ConfigLoader originally built for the controller
 * service. It supports both a production path (reads from classpath, resolves via {@code
 * System.getenv}) and a testable path (accepts an {@code InputStream} and env resolver function).
 */
public class ConfigLoader {

  private final ObjectMapper ymlMapper;

  public ConfigLoader() {
    ymlMapper = new ObjectMapper(new YAMLFactory());
    ymlMapper.registerModule(new JavaTimeModule());
    ymlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
  }

  /** Production entry point -- loads from classpath and resolves env vars via System.getenv. */
  public AppConfig loadAppConfig() {
    return load(getClass().getResourceAsStream("/application.yml"), System::getenv);
  }

  /**
   * Testable entry point -- loads from the given stream using the provided environment resolver.
   *
   * @param input configuration input stream
   * @param environment environment variable resolver
   * @return resolved application configuration
   */
  AppConfig load(InputStream input, Function<String, String> environment) {
    try (input) {
      if (input == null) {
        throw new MissingConfigFileException("Missing application.yml configuration file");
      }

      AppConfig baseConfig = ymlMapper.readValue(input, AppConfig.class);
      return overrideWithEnvironment(baseConfig, environment);

    } catch (IOException e) {
      throw new RuntimeException("Failed to load application configuration", e);
    }
  }

  private static AppConfig overrideWithEnvironment(
      AppConfig config, Function<String, String> environment) {

    NodeConfig node =
        new NodeConfig(
            config.node().nodeId(),
            resolve(environment, "NODE_NAME", config.node().name()),
            resolve(environment, "NODE_TYPE", config.node().type()));

    MqttConfig mqtt =
        new MqttConfig(
            resolve(environment, "MQTT_BROKER_URL", config.mqtt().brokerUrl()),
            resolve(environment, "MQTT_TOPIC", config.mqtt().topic()));

    return new AppConfig(node, mqtt);
  }

  private static String resolve(Function<String, String> environment, String key, String fallback) {
    String value = environment.apply(key);
    return value != null ? value : fallback;
  }
}
