package com.networkmonitor.service;

import com.networkmonitor.entity.HealthStatus;
import com.networkmonitor.monitoring.PingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class HealthAnalyzerServiceTest {

    private HealthAnalyzerService healthAnalyzerService;

    @BeforeEach
    void setUp() {
        healthAnalyzerService = new HealthAnalyzerService();
    }

    @Test
    @DisplayName("evaluateHealth for unreachable PingResult should return CRITICAL")
    void evaluateHealth_Unreachable_ReturnsCritical() {
        PingResult result = new PingResult("192.168.1.1", false, 0.0, 100.0);
        HealthStatus status = healthAnalyzerService.evaluateHealth(result);
        assertThat(status).isEqualTo(HealthStatus.CRITICAL);
    }

    @Test
    @DisplayName("evaluateHealth for packet loss > 5% should return CRITICAL")
    void evaluateHealth_HighPacketLoss_ReturnsCritical() {
        PingResult result = new PingResult("192.168.1.1", true, 15.0, 10.0);
        HealthStatus status = healthAnalyzerService.evaluateHealth(result);
        assertThat(status).isEqualTo(HealthStatus.CRITICAL);
    }

    @Test
    @DisplayName("evaluateHealth for latency > 120ms should return WARNING")
    void evaluateHealth_HighLatency_ReturnsWarning() {
        PingResult result = new PingResult("192.168.1.1", true, 150.0, 0.0);
        HealthStatus status = healthAnalyzerService.evaluateHealth(result);
        assertThat(status).isEqualTo(HealthStatus.WARNING);
    }

    @Test
    @DisplayName("evaluateHealth for normal latency and 0% loss should return HEALTHY")
    void evaluateHealth_Normal_ReturnsHealthy() {
        PingResult result = new PingResult("192.168.1.1", true, 12.5, 0.0);
        HealthStatus status = healthAnalyzerService.evaluateHealth(result);
        assertThat(status).isEqualTo(HealthStatus.HEALTHY);
    }
}
