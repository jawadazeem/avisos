/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import java.util.List;

public record LabelConfig (
    List<String> critical,
    List<String> warning
) {}
