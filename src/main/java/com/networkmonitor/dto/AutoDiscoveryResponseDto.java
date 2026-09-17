package com.networkmonitor.dto;

import java.util.ArrayList;
import java.util.List;

public class AutoDiscoveryResponseDto {
    private List<LocalNetworkDto> networks = new ArrayList<>();
    private List<DiscoveredDeviceDto> devices = new ArrayList<>();
    private int networksScanned;
    private int hostsScanned;
    private int devicesDiscovered;
    private int newDevicesImported;
    private long durationMs;

    public List<LocalNetworkDto> getNetworks() {
        return networks;
    }

    public void setNetworks(List<LocalNetworkDto> networks) {
        this.networks = networks;
    }

    public List<DiscoveredDeviceDto> getDevices() {
        return devices;
    }

    public void setDevices(List<DiscoveredDeviceDto> devices) {
        this.devices = devices;
    }

    public int getNetworksScanned() {
        return networksScanned;
    }

    public void setNetworksScanned(int networksScanned) {
        this.networksScanned = networksScanned;
    }

    public int getHostsScanned() {
        return hostsScanned;
    }

    public void setHostsScanned(int hostsScanned) {
        this.hostsScanned = hostsScanned;
    }

    public int getDevicesDiscovered() {
        return devicesDiscovered;
    }

    public void setDevicesDiscovered(int devicesDiscovered) {
        this.devicesDiscovered = devicesDiscovered;
    }

    public int getNewDevicesImported() {
        return newDevicesImported;
    }

    public void setNewDevicesImported(int newDevicesImported) {
        this.newDevicesImported = newDevicesImported;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
}
