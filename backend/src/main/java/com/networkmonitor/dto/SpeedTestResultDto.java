package com.networkmonitor.dto;

import com.networkmonitor.entity.NetworkSpeedTest;
import java.time.LocalDateTime;

public class SpeedTestResultDto {

    private Long id;
    private Long deviceId;
    private String testType;
    private Double downloadMbps;
    private Double uploadMbps;
    private Double latencyMs;
    private Double jitterMs;
    private Double packetLossPercent;
    private Long durationMs;
    private LocalDateTime testedAt;
    private String status;

    public SpeedTestResultDto() {
    }

    public static SpeedTestResultDto fromEntity(NetworkSpeedTest entity) {
        SpeedTestResultDto dto = new SpeedTestResultDto();
        dto.setId(entity.getId());
        dto.setDeviceId(entity.getDeviceId());
        dto.setTestType(entity.getTestType());
        dto.setDownloadMbps(entity.getDownloadMbps());
        dto.setUploadMbps(entity.getUploadMbps());
        dto.setLatencyMs(entity.getLatencyMs());
        dto.setJitterMs(entity.getJitterMs());
        dto.setPacketLossPercent(entity.getPacketLossPercent());
        dto.setDurationMs(entity.getDurationMs());
        dto.setTestedAt(entity.getTestedAt());
        dto.setStatus(entity.getStatus());
        return dto;
      }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }

    public Double getDownloadMbps() { return downloadMbps; }
    public void setDownloadMbps(Double downloadMbps) { this.downloadMbps = downloadMbps; }

    public Double getUploadMbps() { return uploadMbps; }
    public void setUploadMbps(Double uploadMbps) { this.uploadMbps = uploadMbps; }

    public Double getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Double latencyMs) { this.latencyMs = latencyMs; }

    public Double getJitterMs() { return jitterMs; }
    public void setJitterMs(Double jitterMs) { this.jitterMs = jitterMs; }

    public Double getPacketLossPercent() { return packetLossPercent; }
    public void setPacketLossPercent(Double packetLossPercent) { this.packetLossPercent = packetLossPercent; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public LocalDateTime getTestedAt() { return testedAt; }
    public void setTestedAt(LocalDateTime testedAt) { this.testedAt = testedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
