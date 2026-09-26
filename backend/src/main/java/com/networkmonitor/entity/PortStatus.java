package com.networkmonitor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "port_status")
public class PortStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false)
    private Integer port;

    @Column(nullable = false)
    private String protocol = "TCP";

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PortState status;

    @Column(name = "latency_ms")
    private Double latencyMs;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    public PortStatus() {
    }

    public PortStatus(Device device, Integer port, String protocol, String serviceName, PortState status, Double latencyMs, LocalDateTime checkedAt) {
        this.device = device;
        this.port = port;
        this.protocol = protocol;
        this.serviceName = serviceName;
        this.status = status;
        this.latencyMs = latencyMs;
        this.checkedAt = checkedAt;
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

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public PortState getStatus() {
        return status;
    }

    public void setStatus(PortState status) {
        this.status = status;
    }

    public Double getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Double latencyMs) {
        this.latencyMs = latencyMs;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }
}
