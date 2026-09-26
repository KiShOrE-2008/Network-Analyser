package com.networkmonitor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "monitoring_metrics")
public class MonitoringMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private boolean reachable;

    @Column(name = "latency_ms")
    private Double latencyMs;

    @Column(name = "packet_loss_percent")
    private Double packetLossPercent;

    public MonitoringMetric() {
    }

    public MonitoringMetric(Device device, LocalDateTime timestamp, boolean reachable, Double latencyMs, Double packetLossPercent) {
        this.device = device;
        this.timestamp = timestamp;
        this.reachable = reachable;
        this.latencyMs = latencyMs;
        this.packetLossPercent = packetLossPercent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
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
