package com.networkmonitor.service;

import com.networkmonitor.dto.SnmpMetricDto;
import com.networkmonitor.entity.HealthStatus;
import com.networkmonitor.monitoring.PingResult;
import org.springframework.stereotype.Service;

@Service
public class HealthAnalyzerService {

    public HealthStatus evaluateHealth(PingResult pingResult) {
        return evaluateHealth(pingResult, null);
    }

    public HealthStatus evaluateHealth(PingResult pingResult, SnmpMetricDto snmpMetric) {
        if (pingResult == null || !pingResult.isReachable()) {
            return HealthStatus.CRITICAL;
        }

        if (pingResult.getPacketLossPercent() > 5.0) {
            return HealthStatus.CRITICAL;
        }

        if (snmpMetric != null) {
            if (snmpMetric.getCpuUsagePercent() > 90.0 || snmpMetric.getMemoryUsagePercent() > 95.0) {
                return HealthStatus.CRITICAL;
            }
        }

        if (pingResult.getLatencyMs() > 120.0) {
            return HealthStatus.WARNING;
        }

        if (snmpMetric != null) {
            if (snmpMetric.getCpuUsagePercent() > 80.0 || snmpMetric.getMemoryUsagePercent() > 85.0) {
                return HealthStatus.WARNING;
            }
        }

        return HealthStatus.HEALTHY;
    }
}
