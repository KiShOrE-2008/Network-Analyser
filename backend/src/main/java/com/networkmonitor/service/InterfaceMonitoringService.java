package com.networkmonitor.service;

import com.networkmonitor.dto.InterfaceMetricDto;
import com.networkmonitor.entity.Device;
import com.networkmonitor.entity.InterfaceMetric;
import com.networkmonitor.exception.ResourceNotFoundException;
import com.networkmonitor.monitoring.SnmpService;
import com.networkmonitor.repository.DeviceRepository;
import com.networkmonitor.repository.InterfaceMetricRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterfaceMonitoringService {

    private final InterfaceMetricRepository interfaceMetricRepository;
    private final DeviceRepository deviceRepository;
    private final SnmpService snmpService;

    public InterfaceMonitoringService(
            InterfaceMetricRepository interfaceMetricRepository,
            DeviceRepository deviceRepository,
            SnmpService snmpService) {
        this.interfaceMetricRepository = interfaceMetricRepository;
        this.deviceRepository = deviceRepository;
        this.snmpService = snmpService;
    }

    public List<InterfaceMetricDto> getDeviceInterfaces(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        List<InterfaceMetric> existing = interfaceMetricRepository.findTop50ByDeviceIdOrderBySampledAtDesc(deviceId);
        if (!existing.isEmpty()) {
            return existing.stream().map(InterfaceMetricDto::fromEntity).collect(Collectors.toList());
        }

        // Return existing interface metrics recorded for this device
        return existing.stream().map(InterfaceMetricDto::fromEntity).collect(Collectors.toList());
    }

    public InterfaceMetricDto recordInterfaceMetric(InterfaceMetricDto dto) {
        InterfaceMetric metric = new InterfaceMetric();
        metric.setDeviceId(dto.getDeviceId());
        metric.setInterfaceName(dto.getInterfaceName() != null ? dto.getInterfaceName() : "eth0");
        metric.setRxBytes(dto.getRxBytes());
        metric.setTxBytes(dto.getTxBytes());
        metric.setRxPackets(dto.getRxPackets());
        metric.setTxPackets(dto.getTxPackets());
        metric.setRxErrors(dto.getRxErrors());
        metric.setTxErrors(dto.getTxErrors());
        metric.setRxDrops(dto.getRxDrops());
        metric.setTxDrops(dto.getTxDrops());
        metric.setRxMbps(dto.getRxMbps());
        metric.setTxMbps(dto.getTxMbps());

        interfaceMetricRepository.save(metric);
        return InterfaceMetricDto.fromEntity(metric);
    }
}
