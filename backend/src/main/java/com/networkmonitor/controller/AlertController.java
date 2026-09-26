package com.networkmonitor.controller;

import com.networkmonitor.dto.AlertResponseDto;
import com.networkmonitor.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<List<AlertResponseDto>> getAlerts(
            @RequestParam(required = false, defaultValue = "false") boolean includeResolved) {
        List<AlertResponseDto> alerts = includeResolved
                ? alertService.getAllAlerts()
                : alertService.getActiveAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<AlertResponseDto>> getDeviceAlerts(@PathVariable Long deviceId) {
        List<AlertResponseDto> alerts = alertService.getDeviceAlerts(deviceId);
        return ResponseEntity.ok(alerts);
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<AlertResponseDto> resolveAlert(@PathVariable Long id) {
        AlertResponseDto resolvedAlert = alertService.resolveAlert(id);
        return ResponseEntity.ok(resolvedAlert);
    }
}
