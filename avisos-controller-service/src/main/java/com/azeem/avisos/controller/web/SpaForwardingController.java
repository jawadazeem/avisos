/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards client-side routes to {@code index.html} so that React Router handles them. Without
 * this, navigating directly to {@code /nodes} or {@code /alarms} would return a Spring 404.
 */
@Controller
public class SpaForwardingController {

  @GetMapping(
      value = {
        "/{path:^(?!api|actuator|ws|assets|index\\.html|.*\\..*$).*$}",
        "/{path:^(?!api|actuator|ws|assets|index\\.html|.*\\..*$).*$}/**"
      },
      produces = "text/html")
  public String forward() {
    return "forward:/index.html";
  }
}
