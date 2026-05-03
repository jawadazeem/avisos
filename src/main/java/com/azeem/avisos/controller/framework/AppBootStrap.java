/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.framework;

public class AppBootStrap {
    public void init() {
        AppContainer container = new AppContainer();
        container.init();

        AspectProcessor aspectProcessor = new AspectProcessor(container.classObjectMap);
        aspectProcessor.applyAspects();

        ConfigLoader configLoader = new ConfigLoader();
    }
}
