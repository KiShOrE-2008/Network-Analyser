package com.networkmonitor.monitoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PingServiceTest {

    private PingService pingService;

    @BeforeEach
    void setUp() {
        pingService = new PingService();
    }

    @Test
    @DisplayName("ping loopback (127.0.0.1) should return reachable true with non-null latency")
    void ping_Loopback_ShouldBeReachable() {
        PingResult result = pingService.ping("127.0.0.1", 2, 2);

        assertThat(result).isNotNull();
        assertThat(result.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(result.isReachable()).isTrue();
        assertThat(result.getLatencyMs()).isNotNull();
        assertThat(result.getLatencyMs()).isGreaterThanOrEqualTo(0.0);
        assertThat(result.getPacketLossPercent()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("ping unreachable IP (192.168.254.254) should return reachable false with 100% packet loss")
    void ping_UnreachableIp_ShouldReturnNotReachable() {
        PingResult result = pingService.ping("192.168.254.254", 1, 1);

        assertThat(result).isNotNull();
        assertThat(result.getIpAddress()).isEqualTo("192.168.254.254");
        assertThat(result.isReachable()).isFalse();
        assertThat(result.getPacketLossPercent()).isEqualTo(100.0);
    }
}
