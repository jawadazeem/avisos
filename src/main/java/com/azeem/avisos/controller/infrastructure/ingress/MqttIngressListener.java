/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.ingress;

import com.azeem.avisos.controller.config.MqttConfig;
import com.azeem.avisos.controller.exceptions.ConfigFileMisconfiguredException;
import com.azeem.avisos.controller.exceptions.ConfigFileNotFoundException;
import com.azeem.avisos.controller.exceptions.CriticalInfrastructureException;
import com.azeem.avisos.controller.service.ingress.MqttIngressAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MqttIngressListener implements IngressListener {
    private static final Logger log = LoggerFactory.getLogger(MqttIngressListener.class);
    MqttIngressAdapter mqttIngressAdapter;
    private final ExecutorService executor;
    private MqttClient client;
    private final ObjectMapper ymlMapper;
    private final MqttConfig config;

    /**
     *
     * @param ymlMapper must be configured with YAMLFactory to read application.yml file.
     *               It is recommended to use a singleton ObjectMapper instance across
     *               the application for better performance and resource management.
     */
    public MqttIngressListener(MqttIngressAdapter mqttIngressAdapter,
                               ObjectMapper ymlMapper,
                               MqttConfig config
    ) {
        this.mqttIngressAdapter = mqttIngressAdapter;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.ymlMapper = ymlMapper;
        this.config = config;
    }

    @PostConstruct
    @Override
    public void init() {
        MqttConnectOptions options = new MqttConnectOptions();

        options.setAutomaticReconnect(config.automaticReconnect());
        options.setCleanSession(config.cleanSession());
        options.setConnectionTimeout(config.connectionTimeout());

        try {
            client = new MqttClient(config.broker(), config.controllerClientId());
            client.connect(options);

            log.info("Avisos Hub successfully peered with MQTT Broker at {}", config.broker());
            startListening(config);

        } catch (MqttException e) {
            log.error("Critical: MQTT Peering failed. Ingress is offline.", e);
        }
    }

    @PreDestroy
    @Override
    public void shutdown() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                client.close();
                log.info("Disconnected MQTT client");
            }
        } catch (MqttException e) {
            log.warn("Error while disconnecting MQTT client", e);
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void startListening(MqttConfig config) {
        try {
            client.subscribe(config.topic(), (topic, message) -> {
                executor.submit(() -> mqttIngressAdapter.handle(topic, message));
            });
        } catch (MqttException e) {
            log.warn("MQTT subscription failed for topic avisos/telemetry/#", e);
            throw new IllegalStateException("Cannot start MQTT subscription", e);
        }
    }
}
