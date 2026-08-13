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

    public MetricResponseDto() {
    }

    public static MetricResponseDto fromEntity(MonitoringMetric metric) {
        MetricResponseDto dto = new MetricResponseDto();
        dto.setId(metric.getId());
        dto.setDeviceId(metric.getDevice().getId());
        dto.setTimestamp(metric.getTimestamp());
        dto.setReachable(metric.isReachable());
        dto.setLatencyMs(metric.getLatencyMs());
        dto.setPacketLossPercent(metric.getPacketLossPercent());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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
}
