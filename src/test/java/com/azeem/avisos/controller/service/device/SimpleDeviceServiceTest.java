/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.device;

import com.azeem.avisos.controller.model.device.DeviceRecord;
import com.azeem.avisos.controller.model.device.DeviceStatus;
import com.azeem.avisos.controller.repository.DeviceRepository;
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
public class SimpleDeviceServiceTest {

    @Mock
    DeviceRepository deviceRepository;

    @InjectMocks
    SimpleDeviceService deviceService;

    @Captor
    ArgumentCaptor<String> stringCaptor;

    DeviceRecord sampleDevice;

    @BeforeEach
    void setUp() {
        sampleDevice = new DeviceRecord(
                UUID.randomUUID(),
                "Camera-1",
                "CAMERA",
                DeviceStatus.RESPONSIVE,
                98.2,
                LocalDateTime.now()
        );
    }

    @Test
    void updateDeviceHeartbeat_shouldCallRepositoryWithDeviceUuidString() {
        deviceService.updateDeviceHeartbeat(sampleDevice);

        verify(deviceRepository, times(1)).updateDeviceLastSeen(stringCaptor.capture());
        assertEquals(sampleDevice.uuid().toString(), stringCaptor.getValue());
    }

    @Test
    void registerHeartbeat_shouldCallRepositoryWithUuidString() {
        UUID id = UUID.randomUUID();
        deviceService.registerHeartbeat(id);

        verify(deviceRepository, times(1)).updateDeviceLastSeen(stringCaptor.capture());
        assertEquals(id.toString(), stringCaptor.getValue());
    }

    @Test
    void getRegisteredDevices_shouldReturnUuidsFromRepository() {
        List<String> uuids = List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        when(deviceRepository.getRegisteredDeviceUuids()).thenReturn(uuids);

        List<UUID> result = deviceService.getRegisteredDevices();

        assertEquals(2, result.size());
        assertEquals(UUID.fromString(uuids.get(0)), result.get(0));
        assertEquals(UUID.fromString(uuids.get(1)), result.get(1));
        verify(deviceRepository, times(1)).getRegisteredDeviceUuids();
    }

    @Test
    void checkStaleDevices_shouldCallRepositoryAndLog_whenDevicesMarkedOffline() {
        when(deviceRepository.markStaleDevicesOffline(60)).thenReturn(3);

        deviceService.checkStaleDevices();

        verify(deviceRepository, times(1)).markStaleDevicesOffline(60);
    }

    @Test
    void checkStaleDevices_shouldCallRepositoryAndNotLog_whenNoDevicesMarkedOffline() {
        when(deviceRepository.markStaleDevicesOffline(60)).thenReturn(0);

        deviceService.checkStaleDevices();

        verify(deviceRepository, times(1)).markStaleDevicesOffline(60);
    }
}

