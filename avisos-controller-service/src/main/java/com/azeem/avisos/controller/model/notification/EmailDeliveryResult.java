/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.notification;

public record EmailDeliveryResult(boolean delivered, boolean dryRun, String detail) {}
