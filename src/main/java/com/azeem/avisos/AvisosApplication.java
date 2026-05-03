/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos;

import com.azeem.avisos.controller.framework.AppBootStrap;
import com.azeem.avisos.controller.framework.AppContainer;


/**
 * Uses the AppContainer to initialize and run the Sentinel Application
 */
public class AvisosApplication {
    public static void main(String[] args) {
        AppBootStrap bootStrap = new AppBootStrap();

        bootStrap.init();
    }
}

