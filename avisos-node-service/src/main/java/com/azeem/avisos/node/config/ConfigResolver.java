/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.config;

/**
 * Utility methods for resolving runtime configuration values
 * from environment variables.
 *
 * <p>
 * This class provides lightweight configuration resolution
 * without requiring a framework such as Spring Boot.
 * </p>
 */
public final class ConfigResolver {
    /**
     * Prevents instantiation of this utility class.
     */
    private ConfigResolver() {
    }

    /**
     * Resolves an environment variable by key.
     *
     * <p>
     * If the environment variable does not exist,
     * the provided fallback value is returned instead.
     * </p>
     *
     * @param key      the environment variable name
     * @param fallback the fallback value to use if the variable is absent
     * @return the resolved environment value or the fallback value
     */
    public static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value != null ? value : fallback;
    }
}
