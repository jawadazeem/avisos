/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.node;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.azeem.avisos.controller.config.NodeServiceConfig;
import com.azeem.avisos.controller.entity.node.NodeEntity;
import com.azeem.avisos.controller.model.node.NodeStatus;
import com.azeem.avisos.controller.repository.NodeRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SimpleNodeServiceTest {

  @Mock NodeRepository nodeRepository;

  NodeServiceConfig nodeServiceConfig;

  @InjectMocks SimpleNodeService nodeService;

  private UUID nodeId;
  private NodeEntity sampleEntity;

  @BeforeEach
  void setUp() {
    nodeId = UUID.randomUUID();

    nodeServiceConfig =
        new NodeServiceConfig(
            60, // staleThreshold
            2000 // minHeartbeatIntervalMs
            );

    nodeService = new SimpleNodeService(nodeRepository, nodeServiceConfig);

    sampleEntity =
        new NodeEntity(
            nodeId.toString(),
            "Avisos-Node-01",
            "SENSOR",
            NodeStatus.RESPONSIVE.name(),
            85.0,
            LocalDateTime.now().minusMinutes(5));
  }

  @Test
  void registerHeartbeat_shouldUpdateRepository_whenFirstAttempt() {
    when(nodeRepository.getNodeEntity(nodeId.toString())).thenReturn(sampleEntity);
    nodeService.registerHeartbeat(nodeId);
    verify(nodeRepository, times(1)).updateNodeLastSeen(nodeId.toString());
  }

  @Test
  void registerHeartbeat_shouldRateLimit_whenCalledTooRapidly() {
    when(nodeRepository.getNodeEntity(nodeId.toString())).thenReturn(sampleEntity);

    // First call: Success
    nodeService.registerHeartbeat(nodeId);

    // Second call (Immediate): Should be dropped by floodProtector
    nodeService.registerHeartbeat(nodeId);

    // Verify repository was only hit ONCE for the first valid heartbeat
    verify(nodeRepository, times(1)).updateNodeLastSeen(anyString());
  }

  @Test
  void registerHeartbeat_shouldNotUpdate_whenNodeNotFoundInDbOrCache() {
    when(nodeRepository.getNodeEntity(nodeId.toString())).thenReturn(null);

    nodeService.registerHeartbeat(nodeId);

    // Verify repository update was never called because node doesn't exist
    verify(nodeRepository, never()).updateNodeLastSeen(anyString());
  }

  @Test
  void checkStaleNodes_shouldCleanupRegistryAndFloodMap() {
    // Setup a node that is "stale" (5 minutes ago)
    String uuidStr = nodeId.toString();
    when(nodeRepository.getNodeEntity(uuidStr)).thenReturn(sampleEntity);

    // Pulse once to hydrate the cache
    nodeService.registerHeartbeat(nodeId);

    // Mock repository finding stale nodes
    when(nodeRepository.markStaleNodesOffline(60)).thenReturn(1);

    // Run cleanup. This should remove sampleEntity from activeRegistry
    // because sampleEntity.lastSeen() is 5 minutes old.
    nodeService.checkStaleNodes();

    verify(nodeRepository).markStaleNodesOffline(60);
    reset(nodeRepository);

    when(nodeRepository.getNodeEntity(uuidStr)).thenReturn(sampleEntity);

    SimpleNodeService freshService = new SimpleNodeService(nodeRepository, nodeServiceConfig);
    freshService.registerHeartbeat(nodeId);

    verify(nodeRepository, times(1)).getNodeEntity(uuidStr);
  }

  @Test
  void getRegisteredNodes_shouldMapStringsToUuids() {
    List<String> mockUuids = List.of(nodeId.toString(), UUID.randomUUID().toString());
    when(nodeRepository.getRegisteredNodeUuids()).thenReturn(mockUuids);

    List<UUID> result = nodeService.getRegisteredNodes();

    assertEquals(2, result.size());
    assertTrue(result.contains(nodeId));
  }
}
