package com.networkmonitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DiscoveryRequestDto {

    @NotBlank(message = "Subnet CIDR is required. Example: 192.168.1.0/24")
    @Pattern(
        regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/(1[6-9]|2[0-9]|3[0-2])$",
        message = "Invalid CIDR notation format or unsupported subnet prefix (must be between /16 and /32)"
    )
    private String subnetCidr;

    private String strategy = "PING"; // "PING" or "TCP"

    private Integer timeoutMs = 800;

    private Integer threads = 20;

    public DiscoveryRequestDto() {
    }

    public String getSubnetCidr() {
        return subnetCidr;
    }

    public void setSubnetCidr(String subnetCidr) {
        this.subnetCidr = subnetCidr;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }
}
