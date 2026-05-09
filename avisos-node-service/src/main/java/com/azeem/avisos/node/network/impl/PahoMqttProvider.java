/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.network.impl;

import com.azeem.avisos.node.config.MqttConfig;
import com.azeem.avisos.node.config.NodeConfig;
import com.azeem.avisos.node.network.api.MqttProvider;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * MQTT transport backed by Eclipse Paho.
 */
public class PahoMqttProvider implements MqttProvider {
    private static final Logger log = LoggerFactory.getLogger(PahoMqttProvider.class);
    private static final int QOS_AT_LEAST_ONCE = 1;

    private final MqttConfig mqttConfig;
    private final String clientId;

    private MqttClient client;

    public PahoMqttProvider(MqttConfig mqttConfig, NodeConfig nodeConfig) {
        this.mqttConfig = Objects.requireNonNull(mqttConfig, "mqttConfig");
        Objects.requireNonNull(nodeConfig, "nodeConfig");
        this.clientId = "avisos-node-" + nodeConfig.nodeId();
    }

    @Override
    public synchronized void connect() throws MqttException {
        if (client != null && client.isConnected()) {
            return;
        }

        MqttClient newClient = new MqttClient(
                mqttConfig.brokerUrl(),
                clientId,
                new MemoryPersistence()
        );

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);

        try {
            newClient.connect(options);
        } catch (MqttException e) {
            newClient.close();
            throw e;
        }

        client = newClient;
        log.info(
                "Connected MQTT client: clientId={}, brokerUrl={}",
                clientId,
                mqttConfig.brokerUrl()
        );
    }

    @Override
    public synchronized void publish(String topic, byte[] payload) {
        if (client == null || !client.isConnected()) {
            throw new IllegalStateException("MQTT client is not connected");
        }

        try {
            MqttMessage message = new MqttMessage(
                    Objects.requireNonNull(payload, "payload")
            );
            message.setQos(QOS_AT_LEAST_ONCE);
            message.setRetained(false);

            String resolvedTopic = Objects.requireNonNull(topic, "topic");
            if (resolvedTopic.isBlank()) {
                resolvedTopic = mqttConfig.topic();
            }

            client.publish(resolvedTopic, message);
        } catch (MqttException e) {
            throw new IllegalStateException("Failed to publish MQTT message", e);
        }
    }

    @Override
    public synchronized void disconnect() {
        if (client == null) {
            return;
        }

        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
            log.info("Disconnected MQTT client: clientId={}", clientId);
        } catch (MqttException e) {
            throw new IllegalStateException("Failed to disconnect MQTT client", e);
        } finally {
            client = null;
        }
    }
}
