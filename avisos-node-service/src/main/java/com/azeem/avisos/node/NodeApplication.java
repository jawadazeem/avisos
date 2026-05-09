/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node;

import com.azeem.avisos.node.config.AppConfig;
import com.azeem.avisos.node.config.ConfigLoader;
import com.azeem.avisos.node.hardware.BatteryProvider;
import com.azeem.avisos.node.network.api.MqttProvider;
import com.azeem.avisos.node.network.impl.PahoMqttProvider;
import com.azeem.avisos.node.service.HeartbeatService;
import com.azeem.avisos.node.service.NodeRuntime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.concurrent.CountDownLatch;

public final class NodeApplication {

    private NodeApplication() {
    }

    /**
     * Application entry point.
     *
     * @param args command-line startup arguments
     */
    public static void main(String[] args) throws InterruptedException {
        AppConfig config = ConfigLoader.load();
        BatteryProvider batteryProvider = new BatteryProvider();
        MqttProvider mqttProvider = new PahoMqttProvider(
                config.mqtt(),
                config.node()
        );
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        HeartbeatService heartbeatService = new HeartbeatService(
                mqttProvider,
                batteryProvider,
                config.mqtt(),
                config.node(),
                objectMapper
        );
        NodeRuntime runtime = new NodeRuntime(
                config,
                mqttProvider,
                heartbeatService,
                batteryProvider
        );
        CountDownLatch shutdownLatch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            runtime.stop();
            shutdownLatch.countDown();
        }, "avisos-node-shutdown"));

        runtime.start();
        shutdownLatch.await();
    }
}
