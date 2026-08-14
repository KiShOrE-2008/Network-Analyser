package com.networkmonitor.dto;

import java.util.ArrayList;
import java.util.List;

public class NmapHostResultDto {

    private String ipAddress;
    private String status;
    private String hostname;
    private String osMatch;
    private List<NmapPortResultDto> openPorts = new ArrayList<>();

    public NmapHostResultDto() {
    }

    public NmapHostResultDto(String ipAddress, String status, String hostname, String osMatch) {
        this.ipAddress = ipAddress;
        this.status = status;
        this.hostname = hostname;
        this.osMatch = osMatch;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getOsMatch() {
        return osMatch;
    }

    public void setOsMatch(String osMatch) {
        this.osMatch = osMatch;
    }

    public List<NmapPortResultDto> getOpenPorts() {
        return openPorts;
    }

    public void setOpenPorts(List<NmapPortResultDto> openPorts) {
        this.openPorts = openPorts;
    }
}
