package com.networkmonitor.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PortScanResponseDto {

    private Long deviceId;
    private String deviceName;
    private String ipAddress;
    private int totalScanned;
    private int openPortsCount;
    private LocalDateTime scannedAt;
    private List<PortStatusDto> ports;

    public PortScanResponseDto() {
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getTotalScanned() {
        return totalScanned;
    }

    public void setTotalScanned(int totalScanned) {
        this.totalScanned = totalScanned;
    }

    public int getOpenPortsCount() {
        return openPortsCount;
    }

    public void setOpenPortsCount(int openPortsCount) {
        this.openPortsCount = openPortsCount;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }

    public List<PortStatusDto> getPorts() {
        return ports;
    }

    public void setPorts(List<PortStatusDto> ports) {
        this.ports = ports;
    }
}
