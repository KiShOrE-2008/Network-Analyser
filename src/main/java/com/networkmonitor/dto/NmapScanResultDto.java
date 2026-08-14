package com.networkmonitor.dto;

import java.util.ArrayList;
import java.util.List;

public class NmapScanResultDto {

    private String target;
    private String scanProfile;
    private boolean nmapAvailable;
    private int totalHostsScanned;
    private int hostsUpCount;
    private long executionTimeMs;
    private List<NmapHostResultDto> hosts = new ArrayList<>();

    public NmapScanResultDto() {
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getScanProfile() {
        return scanProfile;
    }

    public void setScanProfile(String scanProfile) {
        this.scanProfile = scanProfile;
    }

    public boolean isNmapAvailable() {
        return nmapAvailable;
    }

    public void setNmapAvailable(boolean nmapAvailable) {
        this.nmapAvailable = nmapAvailable;
    }

    public int getTotalHostsScanned() {
        return totalHostsScanned;
    }

    public void setTotalHostsScanned(int totalHostsScanned) {
        this.totalHostsScanned = totalHostsScanned;
    }

    public int getHostsUpCount() {
        return hostsUpCount;
    }

    public void setHostsUpCount(int hostsUpCount) {
        this.hostsUpCount = hostsUpCount;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public List<NmapHostResultDto> getHosts() {
        return hosts;
    }

    public void setHosts(List<NmapHostResultDto> hosts) {
        this.hosts = hosts;
    }
}
