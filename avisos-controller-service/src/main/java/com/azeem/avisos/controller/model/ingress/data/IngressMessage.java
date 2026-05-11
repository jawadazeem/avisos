/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.ingress.data;

import java.time.Instant;

/**
 * Represents a raw message received from an external source (e.g., MQTT) before it is parsed into a
 * domain-specific format.
 *
 * <p>This record acts as a generic "envelope" that captures transport-level metadata such as the
 * origin topic and the exact moment of arrival, ensuring that the original raw payload is preserved
 * for downstream handlers.
 *
 * @param source The origin of the message (e.g., the MQTT topic string).
 * @param payload The raw, unparsed byte array received from the wire.
 * @param timestamp The instant the message was captured by the system adapter.
 */
public record IngressMessage(String source, byte[] payload, Instant timestamp) {}
