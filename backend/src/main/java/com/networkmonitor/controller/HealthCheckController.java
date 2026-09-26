package com.networkmonitor.controller;

import com.networkmonitor.monitoring.NmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private final NmapService nmapService;
    private final DataSource dataSource;

    public HealthCheckController(NmapService nmapService, DataSource dataSource) {
        this.nmapService = nmapService;
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", "NetScope Telemetry Engine");
        healthInfo.put("version", "1.0.0-SNAPSHOT");

        boolean dbConnected = false;
        try (Connection conn = dataSource.getConnection()) {
            dbConnected = conn != null && conn.isValid(2);
        } catch (Exception e) {
            dbConnected = false;
        }

        healthInfo.put("database", dbConnected ? "Connected" : "Disconnected");
        healthInfo.put("nmapAvailable", nmapService.isNmapAvailable());
        healthInfo.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(healthInfo);
    }
}
