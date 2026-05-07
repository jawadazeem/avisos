/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.node;

import com.azeem.avisos.controller.model.node.NodeRecord;
import com.azeem.avisos.controller.model.node.NodeStatus;
import com.azeem.avisos.controller.repository.NodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SimpleNodeServiceTest {

    @Mock
    NodeRepository nodeRepository;

    @InjectMocks
    SimpleNodeService nodeService;

    @Captor
    ArgumentCaptor<String> stringCaptor;

    NodeRecord sampleNode;

    @BeforeEach
    void setUp() {
        sampleNode = new NodeRecord(
                UUID.randomUUID(),
                "Camera-1",
                "CAMERA",
                NodeStatus.RESPONSIVE,
                98.2,
                LocalDateTime.now()
        );
    }

    @Test
    void updateNodeHeartbeat_shouldCallRepositoryWithNodeUuidString() {
        nodeService.updateNodeHeartbeat(sampleNode);

        verify(nodeRepository, times(1)).updateNodeLastSeen(stringCaptor.capture());
        assertEquals(sampleNode.uuid().toString(), stringCaptor.getValue());
    }

    @Test
    void registerHeartbeat_shouldCallRepositoryWithUuidString() {
        UUID id = UUID.randomUUID();
        nodeService.registerHeartbeat(id);

        verify(nodeRepository, times(1)).updateNodeLastSeen(stringCaptor.capture());
        assertEquals(id.toString(), stringCaptor.getValue());
    }

    @Test
    void getRegisteredNodes_shouldReturnUuidsFromRepository() {
        List<String> uuids = List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        when(nodeRepository.getRegisteredNodeUuids()).thenReturn(uuids);

        List<UUID> result = nodeService.getRegisteredNodes();

        assertEquals(2, result.size());
        assertEquals(UUID.fromString(uuids.get(0)), result.get(0));
        assertEquals(UUID.fromString(uuids.get(1)), result.get(1));
        verify(nodeRepository, times(1)).getRegisteredNodeUuids();
    }

    @Test
    void checkStaleNodes_shouldCallRepositoryAndLog_whenNodesMarkedOffline() {
        when(nodeRepository.markStaleNodesOffline(60)).thenReturn(3);

        nodeService.checkStaleNodes();

        verify(nodeRepository, times(1)).markStaleNodesOffline(60);
    }

    @Test
    void checkStaleNodes_shouldCallRepositoryAndNotLog_whenNoNodesMarkedOffline() {
        when(nodeRepository.markStaleNodesOffline(60)).thenReturn(0);

        nodeService.checkStaleNodes();

        verify(nodeRepository, times(1)).markStaleNodesOffline(60);
    }
}

