/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package infrastructure.logger;

public abstract class LoggerDecorator implements Logger {
    Logger logger;

    public LoggerDecorator(Logger logger) {
        this.logger = logger;
    }
}
