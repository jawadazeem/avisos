/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.framework;

import com.azeem.avisos.controller.config.*;
import com.azeem.avisos.controller.exceptions.ConfigFileMisconfiguredException;
import com.azeem.avisos.controller.exceptions.ConfigFileNotFoundException;
import com.azeem.avisos.controller.exceptions.CriticalInfrastructureException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ConfigLoader {
    // Yml ObjectMapper for loading all configs
    private final ObjectMapper ymlMapper;

    public ConfigLoader() {
        ymlMapper = new ObjectMapper(new YAMLFactory());
        ymlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
    }

    public List<List<String>> loadProblematicLabelsConfig() {
        try (InputStream is = getClass().getResourceAsStream("/problematic-labels.yml")) {
            if (is == null) {
                throw new CriticalInfrastructureException
                        ("CRITICAL: Config file not found in classpath!" +
                                " Cannot start application without security policy details."
                        );
            }

            LabelConfig config = ymlMapper.readValue(is, LabelConfig.class);

            return List.of(config.critical(), config.warning());
        } catch (IOException e) {
            throw new ConfigFileNotFoundException("Failed to parse security policy", e);
        }
    }

    public VisionConfig loadVisionConfig() {
        VisionConfig vision = loadAppConfig().getVision();
        return new VisionConfig(
                env("VISION_API_URL", vision.apiUrl()),
                envDouble("VISION_MIN_CONFIDENCE", vision.minConfidence()),
                envInt("VISION_TIMEOUT_SECONDS", vision.timeoutSeconds())
        );
    }

    public MqttConfig loadMqttConfig() {
        MqttConfig mqtt = loadAppConfig().getMqtt();
        return new MqttConfig(
                env("MQTT_CONTROLLER_CLIENT_ID", mqtt.controllerClientId()),
                env("MQTT_BROKER_URL", mqtt.broker()),
                env("MQTT_TOPIC", mqtt.topic()),
                envInt("MQTT_CONNECTION_TIMEOUT", mqtt.connectionTimeout()),
                envBoolean("MQTT_CLEAN_SESSION", mqtt.cleanSession()),
                envBoolean("MQTT_AUTOMATIC_RECONNECT", mqtt.automaticReconnect())
        );
    }

    public DatabaseConfig loadDBConfig() {
        DatabaseConfig database = loadAppConfig().getDatabase();
        return new DatabaseConfig(env("DATABASE_URL", database.url()));
    }

    public NodeServiceConfig loadNodeServiceConfig() {
        NodeServiceConfig nodeServiceConfig = loadAppConfig().getNodeServiceConfig();
        return new NodeServiceConfig(
                envInt("STALE_THRESHOLD", nodeServiceConfig.staleThreshold()),
                envInt("MIN_HEARTBEAT_INTERVAL_MS", nodeServiceConfig.minHeartbeatIntervalMs())
        );
    }

    private AppConfig loadAppConfig() {
        try (InputStream is = getClass().getResourceAsStream("/application.yml")) {
            if (is == null) {
                throw new CriticalInfrastructureException(
                        "CRITICAL: Config file not found in classpath! " +
                                "Cannot start application without connection details."
                );
            }
            return ymlMapper.readValue(is, AppConfig.class);
        } catch (IOException e) {
            throw new ConfigFileMisconfiguredException("Failed to parse application configuration", e);
        }
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int envInt(String key, int fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank()
                ? fallback
                : Integer.parseInt(value);
    }

    private static double envDouble(String key, double fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank()
                ? fallback
                : Double.parseDouble(value);
    }

    private static boolean envBoolean(String key, boolean fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank()
                ? fallback
                : Boolean.parseBoolean(value);
    }
}
