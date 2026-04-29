/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos;

import com.azeem.avisos.container.AppContainer;
import com.azeem.avisos.sim.SimulationEngine;

/**
 * Uses the AppContainer to initialize and run the Sentinel Application
 */
public class AvisosApplication {
    public static void main(String[] args) {

    }

    private static void run() {
        AppContainer container = new AppContainer();
        container.init();
        container.applyAspects();

        Thread thread = new Thread(container.get(SimulationEngine.class));

        container.get(SimulationEngine.class).run();
    }
}

