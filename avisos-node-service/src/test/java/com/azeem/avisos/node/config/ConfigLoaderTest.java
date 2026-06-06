/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.azeem.avisos.node.exception.MissingConfigFileException;
import com.azeem.avisos.node.framework.ConfigLoader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class ConfigLoaderTest {

  private static final UUID DEFAULT_NODE_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000000");

  private final ConfigLoader configLoader = new ConfigLoader();

  @Test
  void shouldLoadYamlConfig() {
    AppConfig config = configLoader.loadAppConfig();

    assertEquals(DEFAULT_NODE_ID, config.node().nodeId());
    assertEquals("unassigned-node", config.node().name());
    assertEquals("data-acquisition-device", config.node().type());
    assertEquals("tcp://localhost:1883", config.mqtt().brokerUrl());
    assertEquals("avisos/telemetry", config.mqtt().topic());
  }

  @Test
  void shouldOverrideBrokerUrlFromEnvironment() {
    Map<String, String> environment =
        Map.of(
            "MQTT_BROKER_URL", "tcp://broker.internal:1883",
            "MQTT_TOPIC", "avisos/telemetry/override",
            "NODE_NAME", "edge-node-01",
            "NODE_TYPE", "environment-monitor");

    AppConfig config = configLoader.load(yamlStream(), environment::get);

    assertEquals("edge-node-01", config.node().name());
    assertEquals("environment-monitor", config.node().type());
    assertEquals("tcp://broker.internal:1883", config.mqtt().brokerUrl());
    assertEquals("avisos/telemetry/override", config.mqtt().topic());
  }

  @Test
  void shouldThrowWhenYamlMissing() {
    assertThrows(MissingConfigFileException.class, () -> configLoader.load(null, key -> null));
  }

  @Test
  void shouldReturnFallbackWhenEnvMissing() {
    Function<String, String> emptyEnvironment = key -> null;

    AppConfig config = configLoader.load(yamlStream(), emptyEnvironment);

    assertEquals("yaml-node", config.node().name());
    assertEquals("sensor", config.node().type());
    assertEquals("tcp://yaml-broker:1883", config.mqtt().brokerUrl());
    assertEquals("avisos/yaml", config.mqtt().topic());
  }

  private static InputStream yamlStream() {
    String yaml =
        """
                node:
                  node-id: 11111111-1111-1111-1111-111111111111
                  name: yaml-node
                  type: sensor

                mqtt:
                  broker-url: tcp://yaml-broker:1883
                  topic: avisos/yaml
                """;

    return new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
  }
}
