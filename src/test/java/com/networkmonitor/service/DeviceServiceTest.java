package com.networkmonitor.service;

import com.networkmonitor.dto.DeviceRequestDto;
import com.networkmonitor.dto.DeviceResponseDto;
import com.networkmonitor.entity.Device;
import com.networkmonitor.entity.DeviceStatus;
import com.networkmonitor.entity.DeviceType;
import com.networkmonitor.entity.HealthStatus;
import com.networkmonitor.exception.ResourceNotFoundException;
import com.networkmonitor.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    private Device sampleDevice;
    private DeviceRequestDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleDevice = new Device("Core Router", "192.168.1.1", DeviceType.ROUTER);
        sampleDevice.setId(1L);
        sampleDevice.setHostname("router.local");
        sampleDevice.setVendor("Cisco");
        sampleDevice.setModel("ISR4331");
        sampleDevice.setStatus(DeviceStatus.ONLINE);
        sampleDevice.setHealthStatus(HealthStatus.HEALTHY);

        sampleDto = new DeviceRequestDto();
        sampleDto.setName("Core Router");
        sampleDto.setIpAddress("192.168.1.1");
        sampleDto.setDeviceType(DeviceType.ROUTER);
        sampleDto.setHostname("router.local");
        sampleDto.setScanInterval(10);
    }

    @Test
    @DisplayName("createDevice should successfully create and return new device DTO")
    void createDevice_Success() {
        when(deviceRepository.existsByIpAddress("192.168.1.1")).thenReturn(false);
        when(deviceRepository.save(any(Device.class))).thenReturn(sampleDevice);

        DeviceResponseDto response = deviceService.createDevice(sampleDto);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Core Router");
        assertThat(response.getIpAddress()).isEqualTo("192.168.1.1");
        verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    @DisplayName("createDevice should throw IllegalArgumentException when IP already exists")
    void createDevice_DuplicateIp_ThrowsException() {
        when(deviceRepository.existsByIpAddress("192.168.1.1")).thenReturn(true);

        assertThatThrownBy(() -> deviceService.createDevice(sampleDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("createDevice should throw IllegalArgumentException when IP address format is invalid")
    void createDevice_InvalidIp_ThrowsException() {
        sampleDto.setIpAddress("999.999.999.999");

        assertThatThrownBy(() -> deviceService.createDevice(sampleDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid IP address");

        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("getDeviceById should return device DTO when device exists")
    void getDeviceById_Success() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(sampleDevice));

        DeviceResponseDto response = deviceService.getDeviceById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Core Router");
    }

    @Test
    @DisplayName("getDeviceById should throw ResourceNotFoundException when device does not exist")
    void getDeviceById_NotFound_ThrowsException() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getDeviceById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Device not found with id: 99");
    }

    @Test
    @DisplayName("toggleMonitoring should switch monitoringEnabled state")
    void toggleMonitoring_Success() {
        sampleDevice.setMonitoringEnabled(true);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(sampleDevice));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeviceResponseDto response = deviceService.toggleMonitoring(1L);

        assertThat(response.isMonitoringEnabled()).isFalse();
        verify(deviceRepository, times(1)).save(sampleDevice);
    }

    @Test
    @DisplayName("deleteDevice should successfully call repository delete")
    void deleteDevice_Success() {
        when(deviceRepository.existsById(1L)).thenReturn(true);
        doNothing().when(deviceRepository).deleteById(1L);

        deviceService.deleteDevice(1L);

        verify(deviceRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("getAllDevices should pass Specification and return matching list")
    void getAllDevices_Success() {
        when(deviceRepository.findAll(any(Specification.class))).thenReturn(List.of(sampleDevice));

        List<DeviceResponseDto> result = deviceService.getAllDevices("192.168", DeviceType.ROUTER, DeviceStatus.ONLINE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIpAddress()).isEqualTo("192.168.1.1");
    }
}
