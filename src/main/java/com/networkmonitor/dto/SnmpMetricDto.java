package com.networkmonitor.dto;

import java.time.LocalDateTime;

public class SnmpMetricDto {

    private Long deviceId;
    private String deviceIp;
    private Long sysUptimeSeconds;
    private Double cpuUsagePercent;
    private Double memoryUsagePercent;
    private Integer networkInterfacesCount;
    private String community;
    private LocalDateTime checkedAt;

    public SnmpMetricDto() {
    }

    public SnmpMetricDto(Long deviceId, String deviceIp, Long sysUptimeSeconds, Double cpuUsagePercent, Double memoryUsagePercent, Integer networkInterfacesCount, String community) {
        this.deviceId = deviceId;
        this.deviceIp = deviceIp;
        this.sysUptimeSeconds = sysUptimeSeconds;
        this.cpuUsagePercent = cpuUsagePercent;
        this.memoryUsagePercent = memoryUsagePercent;
        this.networkInterfacesCount = networkInterfacesCount;
        this.community = community;
        this.checkedAt = LocalDateTime.now();
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public Long getSysUptimeSeconds() {
        return sysUptimeSeconds;
    }

    public void setSysUptimeSeconds(Long sysUptimeSeconds) {
        this.sysUptimeSeconds = sysUptimeSeconds;
    }

    public Double getCpuUsagePercent() {
        return cpuUsagePercent;
    }

    public void setCpuUsagePercent(Double cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
    }

    public Double getMemoryUsagePercent() {
        return memoryUsagePercent;
    }

    public void setMemoryUsagePercent(Double memoryUsagePercent) {
        this.memoryUsagePercent = memoryUsagePercent;
    }

    public Integer getNetworkInterfacesCount() {
        return networkInterfacesCount;
    }

    public void setNetworkInterfacesCount(Integer networkInterfacesCount) {
        this.networkInterfacesCount = networkInterfacesCount;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }
}
