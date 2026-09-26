package com.networkmonitor.dto;

import java.time.LocalDateTime;

public class SystemReportDto {

    private int totalDevices;
    private int onlineDevices;
    private int offlineDevices;
    private double slaAvailabilityPercent;
    private long totalMetricsCollected;
    private double averageSystemLatencyMs;
    private int activeAlertsCount;
    private int totalAlertsCount;
    private LocalDateTime generatedAt;

    public SystemReportDto() {
        this.generatedAt = LocalDateTime.now();
    }

    public int getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(int totalDevices) {
        this.totalDevices = totalDevices;
    }

    public int getOnlineDevices() {
        return onlineDevices;
    }

    public void setOnlineDevices(int onlineDevices) {
        this.onlineDevices = onlineDevices;
    }

    public int getOfflineDevices() {
        return offlineDevices;
    }

    public void setOfflineDevices(int offlineDevices) {
        this.offlineDevices = offlineDevices;
    }

    public double getSlaAvailabilityPercent() {
        return slaAvailabilityPercent;
    }

    public void setSlaAvailabilityPercent(double slaAvailabilityPercent) {
        this.slaAvailabilityPercent = slaAvailabilityPercent;
    }

    public long getTotalMetricsCollected() {
        return totalMetricsCollected;
    }

    public void setTotalMetricsCollected(long totalMetricsCollected) {
        this.totalMetricsCollected = totalMetricsCollected;
    }

    public double getAverageSystemLatencyMs() {
        return averageSystemLatencyMs;
    }

    public void setAverageSystemLatencyMs(double averageSystemLatencyMs) {
        this.averageSystemLatencyMs = averageSystemLatencyMs;
    }

    public int getActiveAlertsCount() {
        return activeAlertsCount;
    }

    public void setActiveAlertsCount(int activeAlertsCount) {
        this.activeAlertsCount = activeAlertsCount;
    }

    public int getTotalAlertsCount() {
        return totalAlertsCount;
    }

    public void setTotalAlertsCount(int totalAlertsCount) {
        this.totalAlertsCount = totalAlertsCount;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
