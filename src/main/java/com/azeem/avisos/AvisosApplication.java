/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos;

import com.azeem.avisos.controller.container.AppContainer;
import com.azeem.avisos.controller.service.command.CliService;


/**
 * Uses the AppContainer to initialize and run the Sentinel Application
 */
public class AvisosApplication {
    public static void main(String[] args) {
        AppContainer container = new AppContainer();
        container.init();
        System.out.println(container.classObjectMap);
        CliService cli = container.get(CliService.class);
        //cli.run();
    }
}

