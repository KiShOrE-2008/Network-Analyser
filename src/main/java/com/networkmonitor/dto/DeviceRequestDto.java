package com.networkmonitor.dto;

import com.networkmonitor.entity.DeviceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class DeviceRequestDto {

    @NotBlank(message = "Device name is required")
    private String name;

    @NotBlank(message = "IP Address is required")
    @Pattern(
        regexp = "^((25[0-5]|(2[0-4]|1[0-9]|[1-9]?[0-9]))\\.){3}(25[0-5]|(2[0-4]|1[0-9]|[1-9]?[0-9]))$",
        message = "Invalid IPv4 address format"
    )
    private String ipAddress;

    private String hostname;

    @NotNull(message = "Device type is required")
    private DeviceType deviceType;

    private String vendor;

    private String model;

    private Boolean monitoringEnabled = true;

    @Min(value = 1, message = "Scan interval must be at least 1 second")
    private Integer scanInterval = 10;

    public DeviceRequestDto() {
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

    public Boolean getMonitoringEnabled() {
        return monitoringEnabled;
    }

    public void setMonitoringEnabled(Boolean monitoringEnabled) {
        this.monitoringEnabled = monitoringEnabled;
    }

    public Integer getScanInterval() {
        return scanInterval;
    }

    public void setScanInterval(Integer scanInterval) {
        this.scanInterval = scanInterval;
    }
}
