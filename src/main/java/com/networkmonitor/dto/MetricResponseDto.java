package com.networkmonitor.dto;

import com.networkmonitor.entity.MonitoringMetric;
import java.time.LocalDateTime;

public class MetricResponseDto {
    private Long id;
    private Long deviceId;
    private LocalDateTime timestamp;
    private boolean reachable;
    private Double latencyMs;
    private Double packetLossPercent;

    public static MetricResponseDto fromEntity(MonitoringMetric m) {
        MetricResponseDto d = new MetricResponseDto();
        d.id = m.getId();
        if (m.getDevice() != null) {
            d.deviceId = m.getDevice().getId();
        }
        d.timestamp = m.getTimestamp();
        d.reachable = m.isReachable();
        d.latencyMs = m.getLatencyMs();
        d.packetLossPercent = m.getPacketLossPercent();
        return d;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isReachable() { return reachable; }
    public void setReachable(boolean reachable) { this.reachable = reachable; }

    public Double getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Double latencyMs) { this.latencyMs = latencyMs; }

    public Double getPacketLossPercent() { return packetLossPercent; }
    public void setPacketLossPercent(Double packetLossPercent) { this.packetLossPercent = packetLossPercent; }
}
