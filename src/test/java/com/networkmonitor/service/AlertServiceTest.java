package com.networkmonitor.service;

import com.networkmonitor.entity.*;
import com.networkmonitor.monitoring.PingResult;
import com.networkmonitor.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private WebSocketNotificationService notificationService;

    @Mock
    private NotificationService channelNotificationService;

    private AlertService alertService;
    private Device sampleDevice;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(alertRepository, notificationService, channelNotificationService);

        sampleDevice = new Device();
        sampleDevice.setId(1L);
        sampleDevice.setName("Edge Router");
        sampleDevice.setIpAddress("192.168.1.1");
        sampleDevice.setHealthStatus(HealthStatus.HEALTHY);
    }

    @Test
    @DisplayName("processStateTransition HEALTHY -> CRITICAL should create CRITICAL Alert")
    void processStateTransition_HealthyToCritical_CreatesAlert() {
        PingResult result = new PingResult("192.168.1.1", false, 0.0, 100.0);
        when(alertRepository.findByDeviceIdAndIsResolvedFalse(1L)).thenReturn(new ArrayList<>());
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        alertService.processStateTransition(sampleDevice, HealthStatus.HEALTHY, HealthStatus.CRITICAL, result);

        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("processStateTransition CRITICAL -> HEALTHY should auto-resolve open alerts and create RECOVERY alert")
    void processStateTransition_CriticalToHealthy_ResolvesOpenAlerts() {
        PingResult result = new PingResult("192.168.1.1", true, 10.0, 0.0);
        Alert openAlert = new Alert(sampleDevice, AlertType.DEVICE_OFFLINE, AlertSeverity.CRITICAL, "Offline");
        openAlert.setId(5L);

        when(alertRepository.findByDeviceIdAndIsResolvedFalse(1L)).thenReturn(List.of(openAlert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        alertService.processStateTransition(sampleDevice, HealthStatus.CRITICAL, HealthStatus.HEALTHY, result);

        verify(alertRepository, times(2)).save(any(Alert.class));
    }

    @Test
    @DisplayName("processStateTransition with NO health status change should NOT create alerts")
    void processStateTransition_NoStateChange_DoesNothing() {
        PingResult result = new PingResult("192.168.1.1", true, 10.0, 0.0);

        alertService.processStateTransition(sampleDevice, HealthStatus.HEALTHY, HealthStatus.HEALTHY, result);

        verify(alertRepository, never()).save(any(Alert.class));
    }
}
