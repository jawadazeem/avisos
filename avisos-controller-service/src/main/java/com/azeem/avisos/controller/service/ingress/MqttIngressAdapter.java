/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.ingress;

import com.azeem.avisos.controller.model.ingress.data.IngressMessage;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Adapts MQTT messages to the internal IngressMessage format and forwards them to the IngressDataHandler.
 */
public class MqttIngressAdapter {
    private static final Logger log = LoggerFactory.getLogger(MqttIngressAdapter.class);
    private final IngressDataHandler<IngressMessage> handler;

    public MqttIngressAdapter(IngressDataHandler<IngressMessage> handler) {
        this.handler = handler;
    }

    public void handle(String topic, MqttMessage message) {
        handler.handle(new IngressMessage(
                topic,
                message.getPayload(),
                Instant.now()
        ));
    }
}
