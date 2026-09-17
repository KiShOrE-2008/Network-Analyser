package com.networkmonitor.controller;

import com.networkmonitor.monitoring.NmapService;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckController.class);

    private final DataSource dataSource;
    private final NmapService nmapService;

    public HealthCheckController(DataSource dataSource, NmapService nmapService) {
        this.dataSource = dataSource;
        this.nmapService = nmapService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> healthInfo = new HashMap<>();
        boolean dbConnected = false;

        try (Connection conn = dataSource.getConnection()) {
            dbConnected = conn.isValid(2);
        } catch (Exception e) {
            log.warn("Health check database probe failed: {}", e.getMessage());
        }

        healthInfo.put("status", dbConnected ? "UP" : "DEGRADED");
        healthInfo.put("service", "NetScope Telemetry Engine");
        healthInfo.put("version", "1.0.0-SNAPSHOT");
        healthInfo.put("database", dbConnected ? "Connected" : "Disconnected");
        healthInfo.put("nmapAvailable", nmapService.isNmapAvailable());
        healthInfo.put("timestamp", LocalDateTime.now());

        HttpStatus status = dbConnected ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return new ResponseEntity<>(healthInfo, status);
    }
}
