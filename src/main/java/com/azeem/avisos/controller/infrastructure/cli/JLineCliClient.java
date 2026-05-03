/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.cli;

import com.azeem.avisos.controller.exceptions.CriticalInfrastructureException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class JLineCliClient implements CliClient {
        private final Terminal terminal;
        private final LineReader reader;

        public JLineCliClient() {
            this.terminal = getTerminal();
            this.reader = getReader(terminal);
        }

        @PostConstruct
        @Override
        public void init() {
            println("Welcome to AVISOS Shell! Type 'exit' to quit.");
        }

        @PreDestroy
        @Override
        public void shutdown() {
            println("Closing AVISOS Shell...");
            try {
                terminal.writer().println();
                terminal.close();
            } catch (IOException e) {
                throw new CriticalInfrastructureException("Error while closing terminal", e);
            }
        }

        @Override
        public void println(String msg) {
            terminal.writer().println(msg);
            terminal.writer().flush();
        }

        @Override
        public String readLn() {
            return reader.readLine("Avisos> ");
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
}
