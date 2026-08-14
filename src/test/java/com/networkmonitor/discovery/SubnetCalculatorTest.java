package com.networkmonitor.discovery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubnetCalculatorTest {

    @Test
    @DisplayName("getIpAddressesInCidr for 192.168.1.0/30 should return 2 usable IPs (192.168.1.1 and 192.168.1.2)")
    void getIpAddressesInCidr_Slash30() {
        List<String> ips = SubnetCalculator.getIpAddressesInCidr("192.168.1.0/30");

        assertThat(ips).hasSize(2);
        assertThat(ips).containsExactly("192.168.1.1", "192.168.1.2");
    }

    @Test
    @DisplayName("getIpAddressesInCidr for 192.168.1.0/24 should return 254 usable IPs")
    void getIpAddressesInCidr_Slash24() {
        List<String> ips = SubnetCalculator.getIpAddressesInCidr("192.168.1.0/24");

        assertThat(ips).hasSize(254);
        assertThat(ips.get(0)).isEqualTo("192.168.1.1");
        assertThat(ips.get(253)).isEqualTo("192.168.1.254");
    }

    @Test
    @DisplayName("getIpAddressesInCidr for invalid CIDR should throw IllegalArgumentException")
    void getIpAddressesInCidr_InvalidCidr_ThrowsException() {
        assertThatThrownBy(() -> SubnetCalculator.getIpAddressesInCidr("invalid_cidr"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid CIDR format");
    }

    @Test
    @DisplayName("getIpAddressesInCidr for prefix < 16 should throw IllegalArgumentException due to security limits")
    void getIpAddressesInCidr_TooLargeSubnet_ThrowsException() {
        assertThatThrownBy(() -> SubnetCalculator.getIpAddressesInCidr("10.0.0.0/8"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be between /16 and /32");
    }
}
