/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.controller;

import com.azeem.avisos.controller.instrumentation.annotations.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.azeem.avisos.controller.service.controller.api.ControllerService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Custom InvocationHandler for dynamic proxy.
 */
public class PerformanceHandler implements InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(PerformanceHandler.class);
    private final ControllerService securityHubServiceImpl;

    public PerformanceHandler(ControllerService securityHubServiceImpl) {
        this.securityHubServiceImpl = securityHubServiceImpl;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (method.isAnnotationPresent(Timed.class)) {
                Timed timed = method.getAnnotation(Timed.class);
                long startTime = System.nanoTime();
                Object result = method.invoke(securityHubServiceImpl, args);
                long elapsedTime = System.nanoTime() - startTime;
                if (timed.thresholdMs() < elapsedTime/(1_000_000)) {
                    System.out.println(
                            "The operation " + method + " took longer than the set " +
                            "threshold indicating a performance issue."
                    );
                }
                return result;
            } else {
                return method.invoke(securityHubServiceImpl, args);
            }
        } catch (InvocationTargetException e) {
            log.error("e: ", e);
            throw e.getCause();
        }
    }
}
