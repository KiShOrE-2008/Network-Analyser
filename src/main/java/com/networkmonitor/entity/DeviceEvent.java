package com.networkmonitor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="device_events", indexes=@Index(name="idx_device_events_device_time", columnList="device_id,event_time"))
public class DeviceEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="device_id",nullable=false) private Device device;
 @Column(name="event_type",nullable=false,length=40) private String eventType;
 @Column(name="previous_status",length=30) private String previousStatus;
 @Column(name="current_status",length=30) private String currentStatus;
 @Column(nullable=false,length=500) private String message;
 @Column(name="event_time",nullable=false) private LocalDateTime eventTime;
 public DeviceEvent(){}
 public DeviceEvent(Device d,String type,String previous,String current,String message){this.device=d;this.eventType=type;this.previousStatus=previous;this.currentStatus=current;this.message=message;this.eventTime=LocalDateTime.now();}
 public Long getId(){return id;} public Long getDeviceId(){return device==null?null:device.getId();} public String getDeviceName(){return device==null?null:device.getName();} public String getDeviceIp(){return device==null?null:device.getIpAddress();}
 public String getEventType(){return eventType;} public String getPreviousStatus(){return previousStatus;} public String getCurrentStatus(){return currentStatus;} public String getMessage(){return message;} public LocalDateTime getEventTime(){return eventTime;}
}