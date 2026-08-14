package com.networkmonitor.service;

import com.networkmonitor.dto.MetricResponseDto;
import com.networkmonitor.dto.PingCheckResponseDto;
import com.networkmonitor.dto.PortScanResponseDto;
import com.networkmonitor.dto.PortStatusDto;
import com.networkmonitor.entity.Device;
import com.networkmonitor.entity.DeviceStatus;
import com.networkmonitor.entity.HealthStatus;
import com.networkmonitor.entity.MonitoringMetric;
import com.networkmonitor.entity.PortStatus;
import com.networkmonitor.entity.PortState;
import com.networkmonitor.exception.ResourceNotFoundException;
import com.networkmonitor.monitoring.PingResult;
import com.networkmonitor.monitoring.PingService;
import com.networkmonitor.monitoring.PortScannerService;
import com.networkmonitor.repository.DeviceRepository;
import com.networkmonitor.repository.MonitoringMetricRepository;
import com.networkmonitor.repository.PortStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeviceMonitoringService {

    private final DeviceRepository deviceRepository;
    private final MonitoringMetricRepository metricRepository;
    private final PortStatusRepository portStatusRepository;
    private final PingService pingService;
    private final PortScannerService portScannerService;
    private final HealthAnalyzerService healthAnalyzerService;
    private final AlertService alertService;

    public DeviceMonitoringService(
            DeviceRepository deviceRepository,
            MonitoringMetricRepository metricRepository,
            PortStatusRepository portStatusRepository,
            PingService pingService,
            PortScannerService portScannerService,
            HealthAnalyzerService healthAnalyzerService,
            AlertService alertService) {
        this.deviceRepository = deviceRepository;
        this.metricRepository = metricRepository;
        this.portStatusRepository = portStatusRepository;
        this.pingService = pingService;
        this.portScannerService = portScannerService;
        this.healthAnalyzerService = healthAnalyzerService;
        this.alertService = alertService;
    }

    @Transactional
    public PingCheckResponseDto performPingCheck(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        PingResult pingResult = pingService.ping(device.getIpAddress());

        HealthStatus previousHealth = device.getHealthStatus();

        // Update Device Status & Health
        if (pingResult.isReachable()) {
            device.setStatus(DeviceStatus.ONLINE);
            device.setLastSeenAt(pingResult.getTimestamp());
        } else {
            device.setStatus(DeviceStatus.OFFLINE);
        }

        HealthStatus currentHealth = healthAnalyzerService.evaluateHealth(pingResult);
        device.setHealthStatus(currentHealth);
        deviceRepository.save(device);

        // Process State Transitions & Alerting
        alertService.processStateTransition(device, previousHealth, currentHealth, pingResult);

        // Save Metric Entry to Database
        MonitoringMetric metric = new MonitoringMetric(
                device,
                pingResult.getTimestamp(),
                pingResult.isReachable(),
                pingResult.getLatencyMs(),
                pingResult.getPacketLossPercent()
        );
        metricRepository.save(metric);

        // Map to Response DTO
        PingCheckResponseDto dto = new PingCheckResponseDto();
        dto.setDeviceId(device.getId());
        dto.setDeviceName(device.getName());
        dto.setIpAddress(device.getIpAddress());
        dto.setReachable(pingResult.isReachable());
        dto.setLatencyMs(pingResult.getLatencyMs());
        dto.setPacketLossPercent(pingResult.getPacketLossPercent());
        dto.setDeviceStatus(device.getStatus());
        dto.setCheckedAt(pingResult.getTimestamp());

        return dto;
    }

    @Transactional(readOnly = true)
    public List<MetricResponseDto> getDeviceMetrics(Long deviceId) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device not found with id: " + deviceId);
        }

        return metricRepository.findTop50ByDeviceIdOrderByTimestampDesc(deviceId)
                .stream()
                .map(MetricResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PortScanResponseDto performPortScan(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        LocalDateTime scannedAt = LocalDateTime.now();
        List<PortStatus> savedPortStatuses = new ArrayList<>();
        int openCount = 0;

        portStatusRepository.deleteByDeviceId(deviceId);

        for (Map.Entry<Integer, String> entry : PortScannerService.COMMON_PORTS.entrySet()) {
            int port = entry.getKey();
            String serviceName = entry.getValue();

            PortScannerService.PortScanResult scanResult = portScannerService.scanPort(device.getIpAddress(), port, serviceName, 400);

            PortStatus portStatus = new PortStatus(
                    device,
                    port,
                    "TCP",
                    serviceName,
                    scanResult.getState(),
                    scanResult.getLatencyMs(),
                    scannedAt
            );
            savedPortStatuses.add(portStatusRepository.save(portStatus));

            if (scanResult.getState() == PortState.OPEN) {
                openCount++;
            }
        }

        PortScanResponseDto dto = new PortScanResponseDto();
        dto.setDeviceId(device.getId());
        dto.setDeviceName(device.getName());
        dto.setIpAddress(device.getIpAddress());
        dto.setTotalScanned(PortScannerService.COMMON_PORTS.size());
        dto.setOpenPortsCount(openCount);
        dto.setScannedAt(scannedAt);
        dto.setPorts(savedPortStatuses.stream().map(PortStatusDto::fromEntity).collect(Collectors.toList()));

        return dto;
    }

    @Transactional(readOnly = true)
    public List<PortStatusDto> getDevicePorts(Long deviceId) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device not found with id: " + deviceId);
        }

        return portStatusRepository.findByDeviceId(deviceId)
                .stream()
                .map(PortStatusDto::fromEntity)
                .collect(Collectors.toList());
    }
}

