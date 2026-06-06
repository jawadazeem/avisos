/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.azeem.avisos.controller.model.node.NodeRecord;
import com.azeem.avisos.controller.model.node.NodeStatus;
import com.azeem.avisos.controller.service.node.NodeService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class NodeControllerTest {

  @Mock NodeService nodeService;

  @InjectMocks NodeController nodeController;

  private UUID nodeId;
  private NodeRecord sampleNode;

  @BeforeEach
  void setUp() {
    nodeId = UUID.randomUUID();
    sampleNode =
        new NodeRecord(nodeId, "node-01", "MQTT_TELEMETRY_NODE", NodeStatus.RESPONSIVE, 85.0,
            LocalDateTime.now());
  }

  @Test
  void getAllNodes_shouldReturnMappedNodeRecords() {
    when(nodeService.getRegisteredNodes()).thenReturn(List.of(nodeId));
    when(nodeService.getNode(nodeId)).thenReturn(Optional.of(sampleNode));

    List<NodeRecord> result = nodeController.getAllNodes();

    assertEquals(1, result.size());
    assertEquals(sampleNode, result.get(0));
  }

  @Test
  void getAllNodes_shouldFilterOutMissingNodes() {
    UUID missingId = UUID.randomUUID();
    when(nodeService.getRegisteredNodes()).thenReturn(List.of(nodeId, missingId));
    when(nodeService.getNode(nodeId)).thenReturn(Optional.of(sampleNode));
    when(nodeService.getNode(missingId)).thenReturn(Optional.empty());

    List<NodeRecord> result = nodeController.getAllNodes();

    assertEquals(1, result.size());
    assertEquals(nodeId, result.get(0).uuid());
  }

  @Test
  void getAllNodes_shouldReturnEmptyListWhenNoNodesRegistered() {
    when(nodeService.getRegisteredNodes()).thenReturn(List.of());

    List<NodeRecord> result = nodeController.getAllNodes();

    assertTrue(result.isEmpty());
  }

  @Test
  void getNode_shouldReturn200WhenNodeExists() {
    when(nodeService.getNode(nodeId)).thenReturn(Optional.of(sampleNode));

    ResponseEntity<NodeRecord> response = nodeController.getNode(nodeId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(sampleNode, response.getBody());
  }

  @Test
  void getNode_shouldReturn404WhenNodeDoesNotExist() {
    when(nodeService.getNode(nodeId)).thenReturn(Optional.empty());

    ResponseEntity<NodeRecord> response = nodeController.getNode(nodeId);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }
}
