/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import java.util.List;

public class LabelConfig {
    private List<String> critical;
    private List<String> warning;

    public LabelConfig() {}

    public List<String> getCritical() {
        return critical;
    }

    public void setCritical(List<String> critical) {
        this.critical = critical;
    }

    public List<String> getWarning() {
        return warning;
    }

    public void setWarning(List<String> warning) {
        this.warning = warning;
    }
}
