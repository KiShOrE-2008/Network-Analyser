package com.networkmonitor.dto;

import com.networkmonitor.entity.DeviceType;

public class DiscoveredDeviceDto {

    private String ipAddress;
    private String hostname;
    private boolean reachable;
    private String suggestedName;
    private DeviceType suggestedType;
    private boolean alreadyMonitored;

    public DiscoveredDeviceDto() {
    }

    public DiscoveredDeviceDto(String ipAddress, String hostname, boolean reachable, String suggestedName, DeviceType suggestedType, boolean alreadyMonitored) {
        this.ipAddress = ipAddress;
        this.hostname = hostname;
        this.reachable = reachable;
        this.suggestedName = suggestedName;
        this.suggestedType = suggestedType;
        this.alreadyMonitored = alreadyMonitored;
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

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public String getSuggestedName() {
        return suggestedName;
    }

    public void setSuggestedName(String suggestedName) {
        this.suggestedName = suggestedName;
    }

    public DeviceType getSuggestedType() {
        return suggestedType;
    }

    public void setSuggestedType(DeviceType suggestedType) {
        this.suggestedType = suggestedType;
    }

    public boolean isAlreadyMonitored() {
        return alreadyMonitored;
    }

    public void setAlreadyMonitored(boolean alreadyMonitored) {
        this.alreadyMonitored = alreadyMonitored;
    }
}
