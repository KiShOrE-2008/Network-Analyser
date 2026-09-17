package com.networkmonitor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
public class Device {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @Column(name = "ip_address", nullable = false, unique = true) private String ipAddress;
    private String hostname;
    @Enumerated(EnumType.STRING) @Column(name = "device_type", nullable = false) private DeviceType deviceType;
    private String vendor;
    private String model;
    @Column(name = "mac_address") private String macAddress;
    @Column(name = "os_clue") private String osClue;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private DeviceStatus status = DeviceStatus.UNKNOWN;
    @Enumerated(EnumType.STRING) @Column(name = "health_status", nullable = false) private HealthStatus healthStatus = HealthStatus.UNKNOWN;
    @Column(name = "monitoring_enabled", nullable = false) private boolean monitoringEnabled = true;
    @Column(name = "scan_interval", nullable = false) private Integer scanInterval = 10;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "last_seen_at") private LocalDateTime lastSeenAt;

    public Device() {}
    public Device(String name, String ipAddress, DeviceType deviceType) { this.name=name; this.ipAddress=ipAddress; this.deviceType=deviceType; }
    @PrePersist protected void onCreate(){ LocalDateTime now=LocalDateTime.now(); createdAt=now; updatedAt=now; }
    @PreUpdate protected void onUpdate(){ updatedAt=LocalDateTime.now(); }
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getIpAddress(){return ipAddress;} public void setIpAddress(String v){ipAddress=v;}
    public String getHostname(){return hostname;} public void setHostname(String v){hostname=v;}
    public DeviceType getDeviceType(){return deviceType;} public void setDeviceType(DeviceType v){deviceType=v;}
    public String getVendor(){return vendor;} public void setVendor(String v){vendor=v;}
    public String getModel(){return model;} public void setModel(String v){model=v;}
    public String getMacAddress(){return macAddress;} public void setMacAddress(String v){macAddress=v;}
    public String getOsClue(){return osClue;} public void setOsClue(String v){osClue=v;}
    public DeviceStatus getStatus(){return status;} public void setStatus(DeviceStatus v){status=v;}
    public HealthStatus getHealthStatus(){return healthStatus;} public void setHealthStatus(HealthStatus v){healthStatus=v;}
    public boolean isMonitoringEnabled(){return monitoringEnabled;} public void setMonitoringEnabled(boolean v){monitoringEnabled=v;}
    public Integer getScanInterval(){return scanInterval;} public void setScanInterval(Integer v){scanInterval=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
    public LocalDateTime getLastSeenAt(){return lastSeenAt;} public void setLastSeenAt(LocalDateTime v){lastSeenAt=v;}
}
