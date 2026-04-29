/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package exceptions;

public class CommandThrottledException extends RuntimeException {
    public CommandThrottledException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandThrottledException(String message) {
        super(message);
    }

    public CommandThrottledException(Throwable cause) {
        super(cause);
    }

    public CommandThrottledException() {
    }
}
