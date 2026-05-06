/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos;

import com.azeem.avisos.controller.framework.AppBootStrap;


/**
 * Uses the AppBootStrap from DIY framework to initialize and run the Avisos Application
 */
public class AvisosApplication {
    void main() {
        AppBootStrap bootStrap = new AppBootStrap();
        bootStrap.init();
    }
}

