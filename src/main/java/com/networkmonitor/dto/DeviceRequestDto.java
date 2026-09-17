package com.networkmonitor.dto;

import com.networkmonitor.entity.DeviceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class DeviceRequestDto {
    @NotBlank(message="Device name is required") private String name;
    @NotBlank(message="IP Address is required") @Pattern(regexp="^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",message="Invalid IPv4 address format") private String ipAddress;
    private String hostname; @NotNull(message="Device type is required") private DeviceType deviceType; private String vendor;private String model;private String macAddress;private String osClue;private Boolean monitoringEnabled=true;@Min(value=1,message="Scan interval must be at least 1 second")private Integer scanInterval=10;
    public DeviceRequestDto(){} public String getName(){return name;}public void setName(String v){name=v;}public String getIpAddress(){return ipAddress;}public void setIpAddress(String v){ipAddress=v;}public String getHostname(){return hostname;}public void setHostname(String v){hostname=v;}public DeviceType getDeviceType(){return deviceType;}public void setDeviceType(DeviceType v){deviceType=v;}public String getVendor(){return vendor;}public void setVendor(String v){vendor=v;}public String getModel(){return model;}public void setModel(String v){model=v;}public String getMacAddress(){return macAddress;}public void setMacAddress(String v){macAddress=v;}public String getOsClue(){return osClue;}public void setOsClue(String v){osClue=v;}public Boolean getMonitoringEnabled(){return monitoringEnabled;}public void setMonitoringEnabled(Boolean v){monitoringEnabled=v;}public Integer getScanInterval(){return scanInterval;}public void setScanInterval(Integer v){scanInterval=v;}
}
