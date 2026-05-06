/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.framework;


public class AppBootStrap {
    public void init() {
        AppContainer container = new AppContainer();
        AspectProcessor aspectProcessor = new AspectProcessor(container.classObjectMap);
        aspectProcessor.applyAspects();
        container.init();
        ConfigLoader configLoader = new ConfigLoader();
    }
}
