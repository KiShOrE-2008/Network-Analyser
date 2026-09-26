package com.networkmonitor.controller;

import com.networkmonitor.dto.DnsTestRequestDto;
import com.networkmonitor.dto.DnsTestResultDto;
import com.networkmonitor.dto.GatewayStatusDto;
import com.networkmonitor.service.DnsDiagnosticsService;
import com.networkmonitor.service.GatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticsController {

    private final GatewayService gatewayService;
    private final DnsDiagnosticsService dnsDiagnosticsService;

    public DiagnosticsController(GatewayService gatewayService, DnsDiagnosticsService dnsDiagnosticsService) {
        this.gatewayService = gatewayService;
        this.dnsDiagnosticsService = dnsDiagnosticsService;
    }

    @GetMapping("/gateway")
    public ResponseEntity<GatewayStatusDto> getGatewayStatus() {
        return ResponseEntity.ok(gatewayService.detectAndCheckGateway());
    }

    @PostMapping("/dns-test")
    public ResponseEntity<DnsTestResultDto> testDns(@RequestBody(required = false) DnsTestRequestDto request) {
        return ResponseEntity.ok(dnsDiagnosticsService.testDnsResolution(request));
    }
}
