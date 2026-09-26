package com.networkmonitor.dto;

public class SystemSettingsDto {
    private int scanInterval = 10;
    private int discoveryInterval = 60;
    private String snmpCommunity = "public";
    private boolean schedulerEnabled = true;

    public SystemSettingsDto() {
    }

    public SystemSettingsDto(int scanInterval, int discoveryInterval, String snmpCommunity, boolean schedulerEnabled) {
        this.scanInterval = scanInterval;
        this.discoveryInterval = discoveryInterval;
        this.snmpCommunity = snmpCommunity;
        this.schedulerEnabled = schedulerEnabled;
    }

    public int getScanInterval() {
        return scanInterval;
    }

    public void setScanInterval(int scanInterval) {
        this.scanInterval = scanInterval;
    }

    public int getDiscoveryInterval() {
        return discoveryInterval;
    }

    public void setDiscoveryInterval(int discoveryInterval) {
        this.discoveryInterval = discoveryInterval;
    }

    public String getSnmpCommunity() {
        return snmpCommunity;
    }

    public void setSnmpCommunity(String snmpCommunity) {
        this.snmpCommunity = snmpCommunity;
    }

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public void setSchedulerEnabled(boolean schedulerEnabled) {
        this.schedulerEnabled = schedulerEnabled;
    }
}
