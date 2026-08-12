package com.networkmonitor.dto;

import com.networkmonitor.entity.Device;
import com.networkmonitor.entity.DeviceStatus;
import com.networkmonitor.entity.DeviceType;
import com.networkmonitor.entity.HealthStatus;

import java.time.LocalDateTime;

public class DeviceResponseDto {

    private Long id;
    private String name;
    private String ipAddress;
    private String hostname;
    private DeviceType deviceType;
    private String vendor;
    private String model;
    private DeviceStatus status;
    private HealthStatus healthStatus;
    private boolean monitoringEnabled;
    private Integer scanInterval;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastSeenAt;

    public DeviceResponseDto() {
    }

    public static DeviceResponseDto fromEntity(Device device) {
        DeviceResponseDto dto = new DeviceResponseDto();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setIpAddress(device.getIpAddress());
        dto.setHostname(device.getHostname());
        dto.setDeviceType(device.getDeviceType());
        dto.setVendor(device.getVendor());
        dto.setModel(device.getModel());
        dto.setStatus(device.getStatus());
        dto.setHealthStatus(device.getHealthStatus());
        dto.setMonitoringEnabled(device.isMonitoringEnabled());
        dto.setScanInterval(device.getScanInterval());
        dto.setCreatedAt(device.getCreatedAt());
        dto.setUpdatedAt(device.getUpdatedAt());
        dto.setLastSeenAt(device.getLastSeenAt());
        return dto;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
    }

    public void setMonitoringEnabled(boolean monitoringEnabled) {
        this.monitoringEnabled = monitoringEnabled;
    }

    public Integer getScanInterval() {
        return scanInterval;
    }

    public void setScanInterval(Integer scanInterval) {
        this.scanInterval = scanInterval;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
