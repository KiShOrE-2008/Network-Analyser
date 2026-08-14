package com.networkmonitor.monitoring;

import com.networkmonitor.entity.PortState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PortScannerServiceTest {

    private PortScannerService portScannerService;

    @BeforeEach
    void setUp() {
        portScannerService = new PortScannerService();
    }

    @Test
    @DisplayName("resolveServiceName should return mapped service name for standard ports")
    void resolveServiceName_StandardPorts() {
        assertThat(portScannerService.resolveServiceName(22)).isEqualTo("SSH");
        assertThat(portScannerService.resolveServiceName(80)).isEqualTo("HTTP");
        assertThat(portScannerService.resolveServiceName(443)).isEqualTo("HTTPS");
        assertThat(portScannerService.resolveServiceName(5432)).isEqualTo("PostgreSQL");
    }

    @Test
    @DisplayName("scanPort on closed/unused port on 127.0.0.1 should return CLOSED or FILTERED state")
    void scanPort_UnusedPort() {
        PortScannerService.PortScanResult result = portScannerService.scanPort("127.0.0.1", 59999, "TEST", 200);

        assertThat(result).isNotNull();
        assertThat(result.getPort()).isEqualTo(59999);
        assertThat(result.getState()).isIn(PortState.CLOSED, PortState.FILTERED);
    }
}
