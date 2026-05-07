/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node;

import com.azeem.avisos.node.config.AppConfig;
import com.azeem.avisos.node.config.ConfigLoader;

public final class NodeApplication {

    /**
     * Application entry point.
     *
     * @param args command-line startup arguments
     */
    public static void main(String[] args) {
        AppConfig config = ConfigLoader.load();

        System.out.println(config);
    }
}