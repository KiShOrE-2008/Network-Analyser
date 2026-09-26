package com.networkmonitor.dto;

import java.util.List;

public class DiscoveryResponseDto {

    private String subnetCidr;
    private int totalScanned;
    private int devicesDiscoveredCount;
    private int newDevicesCount;
    private int existingDevicesCount;
    private long scanDurationMs;
    private List<DiscoveredDeviceDto> discoveredDevices;

    public DiscoveryResponseDto() {
    }

    public String getSubnetCidr() {
        return subnetCidr;
    }

    public void setSubnetCidr(String subnetCidr) {
        this.subnetCidr = subnetCidr;
    }

    public int getTotalScanned() {
        return totalScanned;
    }

    public void setTotalScanned(int totalScanned) {
        this.totalScanned = totalScanned;
    }

    public int getDevicesDiscoveredCount() {
        return devicesDiscoveredCount;
    }

    public void setDevicesDiscoveredCount(int devicesDiscoveredCount) {
        this.devicesDiscoveredCount = devicesDiscoveredCount;
    }

    public int getNewDevicesCount() {
        return newDevicesCount;
    }

    public void setNewDevicesCount(int newDevicesCount) {
        this.newDevicesCount = newDevicesCount;
    }

    public int getExistingDevicesCount() {
        return existingDevicesCount;
    }

    public void setExistingDevicesCount(int existingDevicesCount) {
        this.existingDevicesCount = existingDevicesCount;
    }

    public long getScanDurationMs() {
        return scanDurationMs;
    }

    public void setScanDurationMs(long scanDurationMs) {
        this.scanDurationMs = scanDurationMs;
    }

    public List<DiscoveredDeviceDto> getDiscoveredDevices() {
        return discoveredDevices;
    }

    public void setDiscoveredDevices(List<DiscoveredDeviceDto> discoveredDevices) {
        this.discoveredDevices = discoveredDevices;
    }
}
