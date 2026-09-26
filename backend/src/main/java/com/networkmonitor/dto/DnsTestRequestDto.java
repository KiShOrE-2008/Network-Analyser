package com.networkmonitor.dto;

public class DnsTestRequestDto {

    private String hostname = "example.com";

    public DnsTestRequestDto() {
    }

    public DnsTestRequestDto(String hostname) {
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
