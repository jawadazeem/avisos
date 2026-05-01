/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.ingress;

/**
 * Interface for handling incoming data from various ingress sources (e.g., MQTT, HTTP).
 * @param <T> The type of message being handled
 */
public interface IngressDataHandler<T> {
    void handle(T message);
}
