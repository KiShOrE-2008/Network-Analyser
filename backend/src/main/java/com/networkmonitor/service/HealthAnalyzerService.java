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
            Double cpu = snmpMetric.getCpuUsagePercent();
            Double mem = snmpMetric.getMemoryUsagePercent();
            if ((cpu != null && cpu > 90.0) || (mem != null && mem > 95.0)) {
                return HealthStatus.CRITICAL;
            }
        }

        if (pingResult.getLatencyMs() != null && pingResult.getLatencyMs() > 120.0) {
            return HealthStatus.WARNING;
        }

        if (snmpMetric != null) {
            Double cpu = snmpMetric.getCpuUsagePercent();
            Double mem = snmpMetric.getMemoryUsagePercent();
            if ((cpu != null && cpu > 80.0) || (mem != null && mem > 85.0)) {
                return HealthStatus.WARNING;
            }
        }

        return HealthStatus.HEALTHY;
    }
}
