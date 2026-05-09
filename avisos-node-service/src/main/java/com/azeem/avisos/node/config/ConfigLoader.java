/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.config;

import com.azeem.avisos.node.exception.MissingConfigFileException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

/**
 * Loads and resolves application configuration.
 *
 * <p>
 * Configuration is loaded from the bundled
 * {@code application.yml} resource and then
 * overridden by environment variables when present.
 * </p>
 */
public final class ConfigLoader {

    /**
     * Prevents instantiation.
     */
    private ConfigLoader() {
    }

    /**
     * Loads the application configuration.
     *
     * @return fully resolved application configuration
     */
    public static AppConfig load() {
        return load(
                ConfigLoader.class.getResourceAsStream("/application.yml"),
                System::getenv
        );
    }

    static AppConfig load(
            InputStream input,
            Function<String, String> environment
    ) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        mapper.registerModule(new JavaTimeModule());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

        try (input) {

            if (input == null) {
                throw new MissingConfigFileException(
                        "Missing application.yml configuration file"
                );
            }

            AppConfig baseConfig =
                    mapper.readValue(input, AppConfig.class);

            return overrideWithEnvironment(baseConfig, environment);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load application configuration",
                    e
            );
        }
    }

    /**
     * Applies environment variable overrides to the base configuration.
     *
     * @param config the base YAML configuration
     * @return resolved configuration with environment overrides
     */
    private static AppConfig overrideWithEnvironment(
            AppConfig config,
            Function<String, String> environment
    ) {

        NodeConfig node = new NodeConfig(
                config.node().nodeId(),
                resolve(environment, "NODE_NAME", config.node().name()),
                resolve(environment, "NODE_TYPE", config.node().type())
        );

        MqttConfig mqtt = new MqttConfig(
                resolve(
                        environment,
                        "MQTT_BROKER_URL",
                        config.mqtt().brokerUrl()
                ),
                resolve(
                        environment,
                        "MQTT_TOPIC",
                        config.mqtt().topic()
                )
        );

        return new AppConfig(node, mqtt);
    }

    private static String resolve(
            Function<String, String> environment,
            String key,
            String fallback
    ) {
        String value = environment.apply(key);
        return value != null ? value : fallback;
    }
}
