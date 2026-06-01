/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.cli;

public record CliResponseMessage(String command, String output, long executionMs) {}
