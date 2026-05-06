/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.device;

import com.azeem.avisos.controller.instrumentation.annotations.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Custom InvocationHandler for dynamic proxy.
 */
public class PerformanceHandler implements InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(PerformanceHandler.class);
    private final DeviceService deviceService;

    public PerformanceHandler(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (method.isAnnotationPresent(Timed.class)) {
                Timed timed = method.getAnnotation(Timed.class);
                long startTime = System.nanoTime();
                Object result = method.invoke(deviceService, args);
                long elapsedTime = System.nanoTime() - startTime;
                if (timed.thresholdMs() < elapsedTime/(1_000_000)) {
                    IO.println(
                            "The operation " + method + " took longer than the set " +
                                    "threshold indicating a performance issue."
                    );
                }
                return result;
            } else {
                return method.invoke(deviceService, args);
            }
        } catch (InvocationTargetException e) {
            log.error("e: ", e);
            throw e.getCause();
        }
    }
}
