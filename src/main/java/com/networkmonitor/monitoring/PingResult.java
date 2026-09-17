package com.networkmonitor.monitoring;

import java.time.LocalDateTime;

public class PingResult {

    private String ipAddress;
    private boolean reachable;
    private Double latencyMs;
    private Double packetLossPercent;
    private LocalDateTime timestamp;
    private String errorMessage;

    public PingResult() {
        this.timestamp = LocalDateTime.now();
    }

    public PingResult(String ipAddress, boolean reachable, Double latencyMs, Double packetLossPercent) {
        this.ipAddress = ipAddress;
        this.reachable = reachable;
        this.latencyMs = latencyMs;
        this.packetLossPercent = packetLossPercent;
        this.timestamp = LocalDateTime.now();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public Double getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Double latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Double getPacketLossPercent() {
        return packetLossPercent;
    }

    public void setPacketLossPercent(Double packetLossPercent) {
        this.packetLossPercent = packetLossPercent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
