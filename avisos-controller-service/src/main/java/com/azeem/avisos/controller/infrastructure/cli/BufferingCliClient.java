/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.cli;

/**
 * A non-interactive {@link CliClient} that captures all {@code println} output into a string
 * buffer. Designed for single-use: create a new instance per command execution, execute the
 * command, then call {@link #getOutput()} to retrieve the captured output.
 *
 * <p>Interactive methods ({@code readLn}, {@code readPassword}) throw {@link
 * UnsupportedOperationException} since WebSocket CLI execution is non-interactive.
 */
public class BufferingCliClient implements CliClient {
  private final StringBuilder buffer = new StringBuilder();

  @Override
  public void println(String msg) {
    buffer.append(msg).append("\n");
  }

  public String getOutput() {
    return buffer.toString();
  }

  @Override
  public void init() {}

  @Override
  public void shutdown() {}

  @Override
  public boolean isRunning() {
    return true;
  }

  @Override
  public String readLn() {
    throw new UnsupportedOperationException("Web CLI does not support interactive input");
  }

  @Override
  public String readLn(String prompt) {
    throw new UnsupportedOperationException("Web CLI does not support interactive input");
  }

  @Override
  public String readPassword() {
    throw new UnsupportedOperationException("Web CLI does not support interactive input");
  }

  @Override
  public String readPassword(String prompt) {
    throw new UnsupportedOperationException("Web CLI does not support interactive input");
  }
}
