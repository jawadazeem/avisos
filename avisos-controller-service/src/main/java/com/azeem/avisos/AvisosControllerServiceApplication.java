/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos;

import com.azeem.avisos.controller.framework.AppLifeCycle;

/** Uses the AppLifeCycle from DIY framework to initialize and run the Avisos Application */
public class AvisosControllerServiceApplication {
  void main() {
    AppLifeCycle lifecycle = new AppLifeCycle();
    lifecycle.init();

    Runtime.getRuntime().addShutdownHook(new Thread(lifecycle::close));
  }
}
