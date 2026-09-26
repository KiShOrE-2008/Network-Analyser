package com.networkmonitor.service;

import com.networkmonitor.discovery.DiscoveryStrategy;
import com.networkmonitor.dto.DeviceRequestDto;
import com.networkmonitor.dto.DeviceResponseDto;
import com.networkmonitor.dto.DiscoveryRequestDto;
import com.networkmonitor.dto.DiscoveryResponseDto;
import com.networkmonitor.entity.DeviceType;
import com.networkmonitor.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscoveryServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceService deviceService;

    @Mock
    private DiscoveryStrategy pingStrategy;

    private DiscoveryService discoveryService;

    @BeforeEach
    void setUp() {
        Map<String, DiscoveryStrategy> strategyMap = new HashMap<>();
        strategyMap.put("PING_DISCOVERY", pingStrategy);
        discoveryService = new DiscoveryService(strategyMap, deviceRepository, deviceService);
    }

    @Test
    @DisplayName("scanSubnet should perform concurrent scanning and return discovery response")
    void scanSubnet_Success() {
        DiscoveryRequestDto request = new DiscoveryRequestDto();
        request.setSubnetCidr("192.168.1.0/30");
        request.setStrategy("PING");
        request.setThreads(2);
        request.setTimeoutMs(100);

        when(pingStrategy.checkReachability(eq("192.168.1.1"), anyInt())).thenReturn(true);
        when(pingStrategy.checkReachability(eq("192.168.1.2"), anyInt())).thenReturn(false);
        when(deviceRepository.existsByIpAddress("192.168.1.1")).thenReturn(false);

        DiscoveryResponseDto response = discoveryService.scanSubnet(request);

        assertThat(response).isNotNull();
        assertThat(response.getTotalScanned()).isEqualTo(2);
        assertThat(response.getDevicesDiscoveredCount()).isEqualTo(1);
        assertThat(response.getNewDevicesCount()).isEqualTo(1);
        assertThat(response.getDiscoveredDevices()).hasSize(1);
        assertThat(response.getDiscoveredDevices().get(0).getIpAddress()).isEqualTo("192.168.1.1");
    }

    @Test
    @DisplayName("importDiscoveredDevices should call deviceService.createDevice for new IPs")
    void importDiscoveredDevices_Success() {
        DeviceRequestDto dto = new DeviceRequestDto();
        dto.setName("Discovered Router");
        dto.setIpAddress("192.168.1.1");
        dto.setDeviceType(DeviceType.ROUTER);

        DeviceResponseDto responseDto = new DeviceResponseDto();
        responseDto.setId(10L);
        responseDto.setIpAddress("192.168.1.1");

        when(deviceRepository.existsByIpAddress("192.168.1.1")).thenReturn(false);
        when(deviceService.createDevice(any(DeviceRequestDto.class))).thenReturn(responseDto);

        List<DeviceResponseDto> imported = discoveryService.importDiscoveredDevices(List.of(dto));

        assertThat(imported).hasSize(1);
        assertThat(imported.get(0).getId()).isEqualTo(10L);
        verify(deviceService, times(1)).createDevice(dto);
    }
}
