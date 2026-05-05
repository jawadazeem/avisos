/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.cli;

import com.azeem.avisos.controller.exceptions.CriticalInfrastructureException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JLineCliClient implements CliClient {
    private static final Logger log = LoggerFactory.getLogger(JLineCliClient.class);
    private final Terminal terminal;
    private final LineReader reader;
    private volatile boolean isRunning;

    public JLineCliClient() {
        this.terminal = getTerminal();
        this.reader = getReader(terminal);
    }

    @Override
    public void init() {
        isRunning = true;
        println("Welcome to AVISOS Shell! Please login.");
    }

    @Override
    public void shutdown() {
        println("Closing AVISOS Shell...");
        isRunning = false;
        try {
            terminal.writer().println();
            terminal.close();
        } catch (IOException e) {
            log.error("Couldn't close terminal properly: {}", e.getMessage());
        }
    }

    @Override
    public synchronized void println(String msg) {
        terminal.writer().println(msg);
        terminal.writer().flush();
    }

    @Override
    public String readLn(String prompt) {
        return reader.readLine(prompt);
    }

    @Override
    public String readLn() {
        return reader.readLine("Avisos> ");
    }

    @Override
    public String readPassword() {
        return reader.readLine("Password: ", '*');
    }

    private Terminal getTerminal() {
        try {
            return TerminalBuilder.builder()
                    .system(true)
                    .build();
        } catch (IOException e) {
            throw new CriticalInfrastructureException("Could not initialize terminal", e);
        }
    }

    private LineReader getReader(Terminal terminal) {
        return LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
