package com.networkmonitor.service;

import com.networkmonitor.dto.AlertResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void dispatchAlertNotification(AlertResponseDto alert) {
        if (alert == null) return;

        String title = String.format("[%s ALERT] %s - %s", alert.getSeverity(), alert.getDeviceName(), alert.getAlertType());
        String body = String.format("Device IP: %s | Message: %s | Time: %s", alert.getDeviceIp(), alert.getMessage(), alert.getCreatedAt());

        // Dispatch Email / Webhook / Syslog channel notification
        log.warn("NOTIFICATION DISPATCH -> Title: '{}' | Details: '{}'", title, body);
    }
}
