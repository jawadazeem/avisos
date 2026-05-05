/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.cli;

public interface CliClient {
    void init();
    void shutdown();
    boolean isRunning();
    void println(String msg);
    String readLn();
    String readPassword();
    String readPassword(String prompt);
    String readLn(String prompt);
}
