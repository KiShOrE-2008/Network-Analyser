package com.networkmonitor.service;

import com.networkmonitor.dto.SystemReportDto;
import com.networkmonitor.entity.Device;
import com.networkmonitor.entity.DeviceStatus;
import com.networkmonitor.entity.MonitoringMetric;
import com.networkmonitor.repository.AlertRepository;
import com.networkmonitor.repository.DeviceRepository;
import com.networkmonitor.repository.MonitoringMetricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReportService {

    private final DeviceRepository deviceRepository;
    private final MonitoringMetricRepository metricRepository;
    private final AlertRepository alertRepository;

    public ReportService(
            DeviceRepository deviceRepository,
            MonitoringMetricRepository metricRepository,
            AlertRepository alertRepository) {
        this.deviceRepository = deviceRepository;
        this.metricRepository = metricRepository;
        this.alertRepository = alertRepository;
    }

    @Transactional(readOnly = true)
    public SystemReportDto generateSystemSummaryReport() {
        List<Device> devices = deviceRepository.findAll();
        long totalMetrics = metricRepository.count();
        long totalAlerts = alertRepository.count();
        long activeAlerts = alertRepository.findByIsResolvedFalseOrderByCreatedAtDesc().size();

        int totalCount = devices.size();
        int onlineCount = (int) devices.stream().filter(d -> d.getStatus() == DeviceStatus.ONLINE).count();
        int offlineCount = totalCount - onlineCount;

        double slaPercent = totalCount > 0 ? (double) onlineCount / totalCount * 100.0 : 100.0;
        slaPercent = Math.round(slaPercent * 10.0) / 10.0;

        List<MonitoringMetric> allMetrics = metricRepository.findAll();
        double avgLatency = allMetrics.stream()
                .filter(m -> m.getLatencyMs() != null)
                .mapToDouble(MonitoringMetric::getLatencyMs)
                .average()
                .orElse(0.0);
        avgLatency = Math.round(avgLatency * 100.0) / 100.0;

        SystemReportDto report = new SystemReportDto();
        report.setTotalDevices(totalCount);
        report.setOnlineDevices(onlineCount);
        report.setOfflineDevices(offlineCount);
        report.setSlaAvailabilityPercent(slaPercent);
        report.setTotalMetricsCollected(totalMetrics);
        report.setAverageSystemLatencyMs(avgLatency);
        report.setActiveAlertsCount((int) activeAlerts);
        report.setTotalAlertsCount((int) totalAlerts);

        return report;
    }

    @Transactional(readOnly = true)
    public String exportDevicesCsv() {
        List<Device> devices = deviceRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Name,IP Address,Hostname,Type,Vendor,Model,Status,Health,Monitoring Enabled,Scan Interval Seconds\n");

        for (Device d : devices) {
            sb.append(d.getId()).append(",")
                    .append(escapeCsv(d.getName())).append(",")
                    .append(escapeCsv(d.getIpAddress())).append(",")
                    .append(escapeCsv(d.getHostname() != null ? d.getHostname() : "")).append(",")
                    .append(d.getDeviceType()).append(",")
                    .append(escapeCsv(d.getVendor() != null ? d.getVendor() : "")).append(",")
                    .append(escapeCsv(d.getModel() != null ? d.getModel() : "")).append(",")
                    .append(d.getStatus()).append(",")
                    .append(d.getHealthStatus()).append(",")
                    .append(d.isMonitoringEnabled()).append(",")
                    .append(d.getScanInterval()).append("\n");
        }

        return sb.toString();
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }
}
