package com.networkmonitor.dto;

import com.networkmonitor.entity.DeviceEvent;
import java.time.LocalDateTime;

public class DeviceEventDto{
 private Long id,deviceId; private String deviceName,deviceIp,eventType,previousStatus,currentStatus,message; private LocalDateTime eventTime;
 public static DeviceEventDto from(DeviceEvent e){DeviceEventDto d=new DeviceEventDto();d.id=e.getId();d.deviceId=e.getDeviceId();d.deviceName=e.getDeviceName();d.deviceIp=e.getDeviceIp();d.eventType=e.getEventType();d.previousStatus=e.getPreviousStatus();d.currentStatus=e.getCurrentStatus();d.message=e.getMessage();d.eventTime=e.getEventTime();return d;}
 public Long getId(){return id;} public Long getDeviceId(){return deviceId;} public String getDeviceName(){return deviceName;} public String getDeviceIp(){return deviceIp;} public String getEventType(){return eventType;} public String getPreviousStatus(){return previousStatus;} public String getCurrentStatus(){return currentStatus;} public String getMessage(){return message;} public LocalDateTime getEventTime(){return eventTime;}
}