/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import com.azeem.avisos.controller.model.node.NodeRecord;
import com.azeem.avisos.controller.service.node.NodeService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for querying registered edge nodes and their current status. */
@RestController
@RequestMapping("/api/nodes")
public class NodeController {
  private final NodeService nodeService;

  public NodeController(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  @GetMapping
  public List<NodeRecord> getAllNodes() {
    return nodeService.getRegisteredNodes().stream()
        .map(nodeService::getNode)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<NodeRecord> getNode(@PathVariable UUID id) {
    return nodeService
        .getNode(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
