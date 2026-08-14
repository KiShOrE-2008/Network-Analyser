package com.networkmonitor.service;

import com.networkmonitor.dto.AlertResponseDto;
import com.networkmonitor.dto.PingCheckResponseDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyMetricUpdate(PingCheckResponseDto pingCheckResponse) {
        try {
            messagingTemplate.convertAndSend("/topic/metrics", pingCheckResponse);
        } catch (Exception ignored) {
        }
    }

    public void notifyAlertUpdate(AlertResponseDto alertResponse) {
        try {
            messagingTemplate.convertAndSend("/topic/alerts", alertResponse);
        } catch (Exception ignored) {
        }
    }

    public void notifyDeviceUpdate(Object deviceResponse) {
        try {
            messagingTemplate.convertAndSend("/topic/devices", deviceResponse);
        } catch (Exception ignored) {
        }
    }
}
