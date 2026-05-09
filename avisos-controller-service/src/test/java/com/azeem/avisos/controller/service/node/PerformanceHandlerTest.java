/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.node;

import com.azeem.avisos.controller.instrumentation.annotations.Timed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceHandlerTest {

    private PerformanceHandler handler;
    private TestNodeService service;

    @BeforeEach
    void setUp() {
        service = new TestNodeService();
        handler = new PerformanceHandler(service);
    }

    @Test
    void shouldInvokeNonTimedMethod() throws Throwable {
        Method method =
                TestNodeService.class.getMethod("normalMethod");

        Object result = handler.invoke(null, method, null);

        assertEquals("normal", result);
        assertTrue(service.normalMethodCalled);
    }

    @Test
    void shouldInvokeTimedMethod() throws Throwable {
        Method method =
                TestNodeService.class.getMethod("timedMethod");

        Object result = handler.invoke(null, method, null);

        assertEquals("timed", result);
        assertTrue(service.timedMethodCalled);
    }

    @Test
    void shouldRethrowOriginalException() throws Throwable {
        Method method =
                TestNodeService.class.getMethod("failingMethod");

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> handler.invoke(null, method, null)
        );

        assertEquals("boom", ex.getMessage());
    }

    static class TestNodeService implements NodeService {

        boolean normalMethodCalled;
        boolean timedMethodCalled;

        public String normalMethod() {
            normalMethodCalled = true;
            return "normal";
        }

        @Timed(thresholdMs = 1)
        public String timedMethod() {
            timedMethodCalled = true;
            return "timed";
        }

        public void failingMethod() {
            throw new RuntimeException("boom");
        }

        // NodeService methods

        @Override
        public void registerHeartbeat(java.util.UUID uuid) {
        }

        @Override
        public void checkStaleNodes() {
        }

        @Override
        public void updateNodeHeartbeat(
                com.azeem.avisos.controller.model.node.NodeRecord nodeRecord
        ) {
        }

        @Override
        public java.util.List<java.util.UUID> getRegisteredNodes() {
            return java.util.List.of();
        }

        @Override
        public java.util.Optional<com.azeem.avisos.controller.model.node.NodeRecord>
        getNode(java.util.UUID nodeId) {
            return java.util.Optional.empty();
        }
    }
}
