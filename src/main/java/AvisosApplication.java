/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

import container.AppContainer;
import core.SecurityHub;
import frontend.LoginFrame;
import infrastructure.repository.UserRepository;
import service.hub.api.SecurityHubService;
import service.receiver.ReceiverService;
import sim.SimulationEngine;

import javax.swing.*;

/**
 * Uses the AppContainer to initialize and run the Sentinel Application
 */
void main() {
    AppContainer container = new AppContainer();
    container.init();
    container.applyAspects();

    Thread thread = new Thread(container.get(SimulationEngine.class));

    container.get(SimulationEngine.class).run();
}