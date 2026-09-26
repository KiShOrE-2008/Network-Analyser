package com.networkmonitor.controller;

import com.networkmonitor.dto.DnsTestRequestDto;
import com.networkmonitor.dto.DnsTestResultDto;
import com.networkmonitor.dto.GatewayStatusDto;
import com.networkmonitor.dto.MetricResponseDto;
import com.networkmonitor.dto.PingCheckResponseDto;
import com.networkmonitor.service.DeviceMonitoringService;
import com.networkmonitor.service.DnsDiagnosticsService;
import com.networkmonitor.service.GatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticsController {

    private final GatewayService gatewayService;
    private final DnsDiagnosticsService dnsDiagnosticsService;
    private final DeviceMonitoringService deviceMonitoringService;

    public DiagnosticsController(
            GatewayService gatewayService,
            DnsDiagnosticsService dnsDiagnosticsService,
            DeviceMonitoringService deviceMonitoringService) {
        this.gatewayService = gatewayService;
        this.dnsDiagnosticsService = dnsDiagnosticsService;
        this.deviceMonitoringService = deviceMonitoringService;
    }

    @GetMapping("/gateway")
    public ResponseEntity<GatewayStatusDto> getGatewayStatus() {
        return ResponseEntity.ok(gatewayService.detectAndCheckGateway());
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<PingCheckResponseDto> getDeviceDiagnostics(@PathVariable Long id) {
        return ResponseEntity.ok(deviceMonitoringService.performPingCheck(id));
    }

    @PostMapping("/dns-test")
    public ResponseEntity<DnsTestResultDto> testDns(@RequestBody(required = false) DnsTestRequestDto request) {
        return ResponseEntity.ok(dnsDiagnosticsService.testDnsResolution(request));
    }
}
