package com.networkmonitor.service;

import com.networkmonitor.entity.HealthStatus;
import com.networkmonitor.monitoring.PingResult;
import org.springframework.stereotype.Service;

@Service
public class HealthAnalyzerService {

    public HealthStatus evaluateHealth(PingResult pingResult) {
        if (pingResult == null || !pingResult.isReachable()) {
            return HealthStatus.CRITICAL;
        }

        if (pingResult.getPacketLossPercent() > 5.0) {
            return HealthStatus.CRITICAL;
        }

        if (pingResult.getLatencyMs() > 120.0) {
            return HealthStatus.WARNING;
        }

        return HealthStatus.HEALTHY;
    }
}
