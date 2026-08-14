package com.networkmonitor.controller;

import com.networkmonitor.dto.MetricResponseDto;
import com.networkmonitor.dto.NmapScanResultDto;
import com.networkmonitor.dto.PingCheckResponseDto;
import com.networkmonitor.dto.PortScanResponseDto;
import com.networkmonitor.dto.PortStatusDto;
import com.networkmonitor.entity.Device;
import com.networkmonitor.exception.ResourceNotFoundException;
import com.networkmonitor.monitoring.NmapService;
import com.networkmonitor.repository.DeviceRepository;
import com.networkmonitor.service.DeviceMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class MonitoringController {

    private final DeviceMonitoringService monitoringService;
    private final DeviceRepository deviceRepository;
    private final NmapService nmapService;

    public MonitoringController(
            DeviceMonitoringService monitoringService,
            DeviceRepository deviceRepository,
            NmapService nmapService) {
        this.monitoringService = monitoringService;
        this.deviceRepository = deviceRepository;
        this.nmapService = nmapService;
    }

    @PostMapping("/{id}/check")
    public ResponseEntity<PingCheckResponseDto> performPingCheck(@PathVariable Long id) {
        PingCheckResponseDto response = monitoringService.performPingCheck(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<List<MetricResponseDto>> getDeviceMetrics(@PathVariable Long id) {
        List<MetricResponseDto> metrics = monitoringService.getDeviceMetrics(id);
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/{id}/scan-ports")
    public ResponseEntity<PortScanResponseDto> performPortScan(@PathVariable Long id) {
        PortScanResponseDto response = monitoringService.performPortScan(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/ports")
    public ResponseEntity<List<PortStatusDto>> getDevicePorts(@PathVariable Long id) {
        List<PortStatusDto> ports = monitoringService.getDevicePorts(id);
        return ResponseEntity.ok(ports);
    }

    @PostMapping("/{id}/nmap-scan")
    public ResponseEntity<NmapScanResultDto> performNmapDeviceScan(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "FAST_PORT") String profile) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        NmapScanResultDto result = nmapService.scanTarget(device.getIpAddress(), profile);
        return ResponseEntity.ok(result);
    }
}


