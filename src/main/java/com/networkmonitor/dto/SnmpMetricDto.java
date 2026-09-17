package com.networkmonitor.dto;

import java.time.LocalDateTime;

public class SnmpMetricDto {
    private Long deviceId; private String deviceIp; private Long sysUptimeSeconds; private Double cpuUsagePercent;
    private Double memoryUsagePercent; private Integer networkInterfacesCount; private String community; private boolean available;
    private String errorMessage; private LocalDateTime checkedAt;
    public SnmpMetricDto() {}
    public SnmpMetricDto(Long id,String ip,Long uptime,Double cpu,Double mem,Integer interfaces,String comm,boolean available,String error){
        deviceId=id;deviceIp=ip;sysUptimeSeconds=uptime;cpuUsagePercent=cpu;memoryUsagePercent=mem;networkInterfacesCount=interfaces;community=comm;this.available=available;errorMessage=error;checkedAt=LocalDateTime.now();
    }
    public Long getDeviceId(){return deviceId;} public void setDeviceId(Long v){deviceId=v;} public String getDeviceIp(){return deviceIp;} public void setDeviceIp(String v){deviceIp=v;}
    public Long getSysUptimeSeconds(){return sysUptimeSeconds;} public void setSysUptimeSeconds(Long v){sysUptimeSeconds=v;} public Double getCpuUsagePercent(){return cpuUsagePercent;} public void setCpuUsagePercent(Double v){cpuUsagePercent=v;}
    public Double getMemoryUsagePercent(){return memoryUsagePercent;} public void setMemoryUsagePercent(Double v){memoryUsagePercent=v;} public Integer getNetworkInterfacesCount(){return networkInterfacesCount;} public void setNetworkInterfacesCount(Integer v){networkInterfacesCount=v;}
    public String getCommunity(){return community;} public void setCommunity(String v){community=v;} public boolean isAvailable(){return available;} public void setAvailable(boolean v){available=v;}
    public String getErrorMessage(){return errorMessage;} public void setErrorMessage(String v){errorMessage=v;} public LocalDateTime getCheckedAt(){return checkedAt;} public void setCheckedAt(LocalDateTime v){checkedAt=v;}
}
