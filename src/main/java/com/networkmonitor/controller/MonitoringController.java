package com.networkmonitor.controller;

import com.networkmonitor.dto.MetricResponseDto;
import com.networkmonitor.dto.PingCheckResponseDto;
import com.networkmonitor.service.DeviceMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class MonitoringController {

    private final DeviceMonitoringService monitoringService;

    public MonitoringController(DeviceMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
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
}
