package com.networkmonitor.dto;

import com.networkmonitor.entity.PortState;
import com.networkmonitor.entity.PortStatus;

import java.time.LocalDateTime;

public class PortStatusDto {

    private Long id;
    private Integer port;
    private String protocol;
    private String serviceName;
    private PortState status;
    private Double latencyMs;
    private LocalDateTime checkedAt;

    public PortStatusDto() {
    }

    public static PortStatusDto fromEntity(PortStatus portStatus) {
        PortStatusDto dto = new PortStatusDto();
        dto.setId(portStatus.getId());
        dto.setPort(portStatus.getPort());
        dto.setProtocol(portStatus.getProtocol());
        dto.setServiceName(portStatus.getServiceName());
        dto.setStatus(portStatus.getStatus());
        dto.setLatencyMs(portStatus.getLatencyMs());
        dto.setCheckedAt(portStatus.getCheckedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
