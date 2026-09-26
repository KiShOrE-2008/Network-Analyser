package com.networkmonitor.service;

import com.networkmonitor.dto.AlertResponseDto;
import com.networkmonitor.entity.*;
import com.networkmonitor.exception.ResourceNotFoundException;
import com.networkmonitor.monitoring.PingResult;
import com.networkmonitor.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final WebSocketNotificationService notificationService;
    private final NotificationService channelNotificationService;

    public AlertService(
            AlertRepository alertRepository,
            WebSocketNotificationService notificationService,
            NotificationService channelNotificationService) {
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
        this.channelNotificationService = channelNotificationService;
    }

    @Transactional
    public void processStateTransition(Device device, HealthStatus previousHealth, HealthStatus currentHealth, PingResult pingResult) {
        if (previousHealth == currentHealth) {
            // No state change -> avoid spamming duplicate alerts
            return;
        }

        if (currentHealth == HealthStatus.HEALTHY) {
            // Device recovered -> Auto-resolve all open alerts for this device
            List<Alert> openAlerts = alertRepository.findByDeviceIdAndIsResolvedFalse(device.getId());
            LocalDateTime now = LocalDateTime.now();

            for (Alert openAlert : openAlerts) {
                openAlert.setResolved(true);
                openAlert.setResolvedAt(now);
                Alert savedAlert = alertRepository.save(openAlert);
                AlertResponseDto dto = mapToDto(savedAlert);
                notificationService.notifyAlertUpdate(dto);
            }

            // Create recovery event
            Alert recoveryAlert = new Alert(
                    device,
                    AlertType.RECOVERY,
                    AlertSeverity.INFO,
                    "Device " + device.getName() + " (" + device.getIpAddress() + ") state recovered to HEALTHY."
            );
            recoveryAlert.setResolved(true);
            recoveryAlert.setResolvedAt(now);
            Alert savedRecovery = alertRepository.save(recoveryAlert);
            AlertResponseDto recDto = mapToDto(savedRecovery);
            notificationService.notifyAlertUpdate(recDto);
            channelNotificationService.dispatchAlertNotification(recDto);

        } else if (currentHealth == HealthStatus.CRITICAL) {
            AlertType type = (!pingResult.isReachable()) ? AlertType.DEVICE_OFFLINE : AlertType.PACKET_LOSS;
            String msg = (!pingResult.isReachable())
                    ? "Device " + device.getName() + " (" + device.getIpAddress() + ") is UNREACHABLE/OFFLINE."
                    : String.format("High packet loss detected on %s (%s): %.1f%%", device.getName(), device.getIpAddress(), pingResult.getPacketLossPercent());

            createAlertIfNotExists(device, type, AlertSeverity.CRITICAL, msg);

        } else if (currentHealth == HealthStatus.WARNING) {
            AlertType type = AlertType.HIGH_LATENCY;
            String msg = String.format("High ping latency detected on %s (%s): %.2f ms", device.getName(), device.getIpAddress(), pingResult.getLatencyMs());

            createAlertIfNotExists(device, type, AlertSeverity.WARNING, msg);
        }
    }

    private void createAlertIfNotExists(Device device, AlertType type, AlertSeverity severity, String message) {
        List<Alert> openAlerts = alertRepository.findByDeviceIdAndIsResolvedFalse(device.getId());
        boolean existsSameType = openAlerts.stream().anyMatch(a -> a.getAlertType() == type);

        if (!existsSameType) {
            Alert alert = new Alert(device, type, severity, message);
            Alert saved = alertRepository.save(alert);
            AlertResponseDto dto = mapToDto(saved);
            notificationService.notifyAlertUpdate(dto);
            channelNotificationService.dispatchAlertNotification(dto);
        }
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDto> getActiveAlerts() {
        return alertRepository.findByIsResolvedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDto> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDto> getDeviceAlerts(Long deviceId) {
        return alertRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlertResponseDto resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));

        if (!alert.isResolved()) {
            alert.setResolved(true);
            alert.setResolvedAt(LocalDateTime.now());
            alert = alertRepository.save(alert);
        }

        return mapToDto(alert);
    }

    private AlertResponseDto mapToDto(Alert alert) {
        if (alert == null) {
            return null;
        }
        AlertResponseDto dto = new AlertResponseDto();
        dto.setId(alert.getId());
        dto.setDeviceId(alert.getDevice().getId());
        dto.setDeviceName(alert.getDevice().getName());
        dto.setDeviceIp(alert.getDevice().getIpAddress());
        dto.setAlertType(alert.getAlertType());
        dto.setSeverity(alert.getSeverity());
        dto.setMessage(alert.getMessage());
        dto.setResolved(alert.isResolved());
        dto.setCreatedAt(alert.getCreatedAt());
        dto.setResolvedAt(alert.getResolvedAt());
        return dto;
    }
}
