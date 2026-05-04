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
    // ObjectMappers for loading all configs
    private ObjectMapper jsonMapper;
    private ObjectMapper ymlMapper;

    public ConfigLoader() {
        jsonMapper = new ObjectMapper();
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
        try (InputStream is = getClass().getResourceAsStream("/application.yml")) {
            if (is == null) {
                throw new CriticalInfrastructureException(
                        "CRITICAL: Config file not found in classpath! Cannot start "
                                + "application without vision service connection details."
                );
            }
            AppConfig config = ymlMapper.readValue(is, AppConfig.class);
            return config.getVision();
        } catch (IOException e) {
            throw new ConfigFileMisconfiguredException("Failed to parse vision policy", e);
        }
    }

    public MqttConfig loadMqttConfig() {
        try (InputStream is = getClass().getResourceAsStream("/application.yml")) {
            if (is == null) {
                throw new CriticalInfrastructureException(
                        "CRITICAL: Config file not found in classpath!" +
                                " Cannot start application without MQTT connection details."
                );
            }
            AppConfig config = ymlMapper.readValue(is, AppConfig.class);
            return config.getMqtt();
        } catch (IOException e) {
            throw new ConfigFileMisconfiguredException("Failed to parse mqtt policy", e);
        }
    }

    public DatabaseConfig loadDBConfig() {
        try (InputStream is = getClass().getResourceAsStream("/application.yml")) {
            if (is == null) {
                throw new CriticalInfrastructureException(
                        "CRITICAL: Config file not found in classpath! " +
                                "Cannot start application without DB connection details."
                );
            }
            AppConfig config = ymlMapper.readValue(is, AppConfig.class);
            return config.getDatabase();
        } catch (IOException e) {
            throw new ConfigFileMisconfiguredException("Failed to parse database policy", e);
        }
    }
}
