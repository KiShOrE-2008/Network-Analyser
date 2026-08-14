package com.networkmonitor.monitoring;

import com.networkmonitor.dto.SnmpMetricDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SnmpServiceTest {

    private SnmpService snmpService;

    @BeforeEach
    void setUp() {
        snmpService = new SnmpService();
    }

    @Test
    @DisplayName("querySnmpMetrics should calculate valid CPU, memory, and uptime metrics")
    void querySnmpMetrics_Success() {
        SnmpMetricDto metrics = snmpService.querySnmpMetrics(1L, "192.168.1.1", "public");

        assertThat(metrics).isNotNull();
        assertThat(metrics.getDeviceId()).isEqualTo(1L);
        assertThat(metrics.getDeviceIp()).isEqualTo("192.168.1.1");
        assertThat(metrics.getCommunity()).isEqualTo("public");
        assertThat(metrics.getCpuUsagePercent()).isBetween(0.0, 100.0);
        assertThat(metrics.getMemoryUsagePercent()).isBetween(0.0, 100.0);
        assertThat(metrics.getSysUptimeSeconds()).isGreaterThan(0L);
        assertThat(metrics.getNetworkInterfacesCount()).isGreaterThan(0);
    }
}
