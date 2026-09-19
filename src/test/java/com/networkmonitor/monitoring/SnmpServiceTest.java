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
    @DisplayName("querySnmpMetrics should return fallback metrics when target SNMP agent is unreachable")
    void querySnmpMetrics_Unreachable_Fallback() {
        SnmpMetricDto metrics = snmpService.querySnmpMetrics(1L, "127.0.0.1", "public");

        assertThat(metrics).isNotNull();
        assertThat(metrics.getDeviceId()).isEqualTo(1L);
        assertThat(metrics.getDeviceIp()).isEqualTo("127.0.0.1");
        assertThat(metrics.getCommunity()).isEqualTo("public");
        assertThat(metrics.getCpuUsagePercent()).isNull();
        assertThat(metrics.getMemoryUsagePercent()).isNull();
        assertThat(metrics.getSysUptimeSeconds()).isNull();
    }
}
