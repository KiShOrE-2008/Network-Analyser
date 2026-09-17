package com.networkmonitor.dto;

import java.time.LocalDateTime;

public class SnmpMetricDto {

    private Long deviceId;
    private String deviceIp;
    private long sysUptimeSeconds;
    private double cpuUsagePercent;
    private double memoryUsagePercent;
    private int networkInterfacesCount;
    private String community;
    private LocalDateTime checkedAt;

    public SnmpMetricDto() {
    }

    public SnmpMetricDto(Long deviceId, String deviceIp, long sysUptimeSeconds, double cpuUsagePercent, double memoryUsagePercent, int networkInterfacesCount, String community) {
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

    public long getSysUptimeSeconds() {
        return sysUptimeSeconds;
    }

    public void setSysUptimeSeconds(long sysUptimeSeconds) {
        this.sysUptimeSeconds = sysUptimeSeconds;
    }

    public double getCpuUsagePercent() {
        return cpuUsagePercent;
    }

    public void setCpuUsagePercent(double cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
    }

    public double getMemoryUsagePercent() {
        return memoryUsagePercent;
    }

    public void setMemoryUsagePercent(double memoryUsagePercent) {
        this.memoryUsagePercent = memoryUsagePercent;
    }

    public int getNetworkInterfacesCount() {
        return networkInterfacesCount;
    }

    public void setNetworkInterfacesCount(int networkInterfacesCount) {
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
