package com.networkmonitor.service;

import com.networkmonitor.dto.MetricResponseDto;
import com.networkmonitor.dto.PingCheckResponseDto;
import com.networkmonitor.entity.Device;
import com.networkmonitor.entity.DeviceStatus;
import com.networkmonitor.entity.DeviceType;
import com.networkmonitor.entity.MonitoringMetric;
import com.networkmonitor.exception.ResourceNotFoundException;
import com.networkmonitor.monitoring.PingResult;
import com.networkmonitor.monitoring.PingService;
import com.networkmonitor.repository.DeviceRepository;
import com.networkmonitor.repository.MonitoringMetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceMonitoringServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private MonitoringMetricRepository metricRepository;

    @Mock
    private PingService pingService;

    @InjectMocks
    private DeviceMonitoringService deviceMonitoringService;

    private Device sampleDevice;

    @BeforeEach
    void setUp() {
        sampleDevice = new Device("Core Router", "192.168.1.1", DeviceType.ROUTER);
        sampleDevice.setId(1L);
    }

    @Test
    @DisplayName("performPingCheck should update device status to ONLINE when ping succeeds")
    void performPingCheck_Online_Success() {
        PingResult pingResult = new PingResult("192.168.1.1", true, 2.5, 0.0);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(sampleDevice));
        when(pingService.ping("192.168.1.1")).thenReturn(pingResult);
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));
        when(metricRepository.save(any(MonitoringMetric.class))).thenAnswer(inv -> inv.getArgument(0));

        PingCheckResponseDto response = deviceMonitoringService.performPingCheck(1L);

        assertThat(response).isNotNull();
        assertThat(response.isReachable()).isTrue();
        assertThat(response.getLatencyMs()).isEqualTo(2.5);
        assertThat(response.getDeviceStatus()).isEqualTo(DeviceStatus.ONLINE);
        verify(deviceRepository, times(1)).save(sampleDevice);
        verify(metricRepository, times(1)).save(any(MonitoringMetric.class));
    }

    @Test
    @DisplayName("performPingCheck should update device status to OFFLINE when ping fails")
    void performPingCheck_Offline_Success() {
        PingResult pingResult = new PingResult("192.168.1.1", false, null, 100.0);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(sampleDevice));
        when(pingService.ping("192.168.1.1")).thenReturn(pingResult);

        PingCheckResponseDto response = deviceMonitoringService.performPingCheck(1L);

        assertThat(response.isReachable()).isFalse();
        assertThat(response.getDeviceStatus()).isEqualTo(DeviceStatus.OFFLINE);
        assertThat(sampleDevice.getStatus()).isEqualTo(DeviceStatus.OFFLINE);
    }

    @Test
    @DisplayName("performPingCheck should throw ResourceNotFoundException when device does not exist")
    void performPingCheck_NotFound_ThrowsException() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceMonitoringService.performPingCheck(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Device not found with id: 99");
    }

    @Test
    @DisplayName("getDeviceMetrics should return metric history list")
    void getDeviceMetrics_Success() {
        MonitoringMetric metric = new MonitoringMetric(sampleDevice, LocalDateTime.now(), true, 1.8, 0.0);
        metric.setId(10L);

        when(deviceRepository.existsById(1L)).thenReturn(true);
        when(metricRepository.findTop50ByDeviceIdOrderByTimestampDesc(1L)).thenReturn(List.of(metric));

        List<MetricResponseDto> metrics = deviceMonitoringService.getDeviceMetrics(1L);

        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).getLatencyMs()).isEqualTo(1.8);
    }
}
