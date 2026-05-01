/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.ingress;

import com.azeem.avisos.controller.model.IngressData.IngressMessage;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class MqttIngressDataHandler {
    private static final Logger log = LoggerFactory.getLogger(MqttIngressDataHandler.class);
    private final IngressDataHandler<IngressMessage> handler;

    public MqttIngressDataHandler(IngressDataHandler<IngressMessage> handler) {
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
