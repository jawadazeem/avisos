/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans registered beans for annotated methods and logs the instrumentation points found.
 *
 * <p>This is scaffolding ported from the controller service. The node service does not yet define
 * custom annotations, but the infrastructure is in place for future use (e.g. adding {@code @Timed}
 * or {@code @ServiceAudit} to node service methods).
 */
public class AspectProcessor {
  private static final Logger log = LoggerFactory.getLogger(AspectProcessor.class);
  private final Map<Class<?>, Object> classObjectMap;

  public AspectProcessor(Map<Class<?>, Object> classObjectMap) {
    this.classObjectMap = classObjectMap;
  }

  /** Scans all registered beans and logs any instrumentation annotations found. */
  public void applyAspects() {
    System.out.println("\n[ Conducting Avisos Node Audit Scan ]");

    int annotationsFound = 0;
    for (Object instance : classObjectMap.values()) {
      Class<?> clazz = instance.getClass();
      for (Method m : clazz.getDeclaredMethods()) {
        for (Annotation a : m.getAnnotations()) {
          String annotationName = a.annotationType().getSimpleName();
          System.out.println(
              "[AUDIT POINT] "
                  + clazz.getSimpleName()
                  + " -> "
                  + m.getName()
                  + " (@"
                  + annotationName
                  + ")");
          annotationsFound++;
        }
      }
    }

    if (annotationsFound == 0) {
      System.out.println("[ No instrumentation annotations detected ]");
    }
    System.out.println("[ Audit Scan Complete ]\n");
  }
}
