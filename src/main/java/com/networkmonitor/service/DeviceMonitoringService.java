package com.networkmonitor.service;

import com.networkmonitor.dto.MetricResponseDto;
import com.networkmonitor.dto.PingCheckResponseDto;
import com.networkmonitor.entity.Device;
import com.networkmonitor.entity.DeviceStatus;
import com.networkmonitor.entity.MonitoringMetric;
import com.networkmonitor.exception.ResourceNotFoundException;
import com.networkmonitor.monitoring.PingResult;
import com.networkmonitor.monitoring.PingService;
import com.networkmonitor.repository.DeviceRepository;
import com.networkmonitor.repository.MonitoringMetricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceMonitoringService {

    private final DeviceRepository deviceRepository;
    private final MonitoringMetricRepository metricRepository;
    private final PingService pingService;

    public DeviceMonitoringService(
            DeviceRepository deviceRepository,
            MonitoringMetricRepository metricRepository,
            PingService pingService) {
        this.deviceRepository = deviceRepository;
        this.metricRepository = metricRepository;
        this.pingService = pingService;
    }

    @Transactional
    public PingCheckResponseDto performPingCheck(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        PingResult pingResult = pingService.ping(device.getIpAddress());

        // Update Device Status
        if (pingResult.isReachable()) {
            device.setStatus(DeviceStatus.ONLINE);
            device.setLastSeenAt(pingResult.getTimestamp());
        } else {
            device.setStatus(DeviceStatus.OFFLINE);
        }
        deviceRepository.save(device);

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
}
