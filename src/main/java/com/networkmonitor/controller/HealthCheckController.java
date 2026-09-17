package com.networkmonitor.controller;

import com.networkmonitor.monitoring.NmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private final NmapService nmapService;

    public HealthCheckController(NmapService nmapService) {
        this.nmapService = nmapService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", "NetScope Telemetry Engine");
        healthInfo.put("version", "1.0.0-SNAPSHOT");
        healthInfo.put("database", "Connected");
        healthInfo.put("nmapAvailable", nmapService.isNmapAvailable());
        healthInfo.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(healthInfo);
    }
}
