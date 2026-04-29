/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos;

import com.azeem.avisos.controller.container.AppContainer;

import org.jline.reader.*;
import org.jline.terminal.*;

import java.io.IOException;

/**
 * Uses the AppContainer to initialize and run the Sentinel Application
 */
public class AvisosApplication {
    public static void main(String[] args) {
        try {
            // Create a terminal
            Terminal terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();

            // Create line reader
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();

            System.out.println("\nWelcome to AVISOS Shell! Type 'exit' to quit.");

            while (true) {
                String line = reader.readLine("Avisos> ");

                // Exit if requested
                if ("exit".equalsIgnoreCase(line)) {
                    break;
                }

                // Echo the line back to the user
                terminal.writer().println("You entered: " + line);
                terminal.flush();
            }

            terminal.writer().println("Closing AVISOS Shell...");
            terminal.close();
        } catch (UserInterruptException e) {
            System.out.println("\nClosing AVISOS Shell...");
        } catch (EndOfFileException e) {
            System.out.println("\nClosing AVISOS Shell...");
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Error creating terminal: " + e.getMessage());
        }

        run();
    }

    private static void run() {
        AppContainer container = new AppContainer();
        container.init();
        container.applyAspects();
    }
}

