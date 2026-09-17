package com.networkmonitor.service;

import com.networkmonitor.entity.Device;
import com.networkmonitor.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringSchedulerServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMonitoringService monitoringService;

    private MonitoringSchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        schedulerService = new MonitoringSchedulerService(deviceRepository, monitoringService);
    }

    @Test
    @DisplayName("executeScheduledMonitoringCycle should scan active devices when scheduler is active")
    void executeScheduledMonitoringCycle_Active() {
        Device device1 = new Device();
        device1.setId(10L);
        device1.setMonitoringEnabled(true);

        when(deviceRepository.findByMonitoringEnabledTrue()).thenReturn(List.of(device1));

        schedulerService.executeScheduledMonitoringCycle();

        assertThat(schedulerService.getLastDevicesScannedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("stopScheduler should deactivate scheduled execution")
    void stopScheduler_DeactivatesCycle() {
        schedulerService.stopScheduler();

        assertThat(schedulerService.isSchedulerActive()).isFalse();

        schedulerService.executeScheduledMonitoringCycle();

        verify(deviceRepository, never()).findByMonitoringEnabledTrue();
    }
}
