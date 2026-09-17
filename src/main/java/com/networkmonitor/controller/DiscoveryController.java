package com.networkmonitor.controller;

import com.networkmonitor.dto.AutoDiscoveryResponseDto;
import com.networkmonitor.dto.DeviceRequestDto;
import com.networkmonitor.dto.DeviceResponseDto;
import com.networkmonitor.dto.DiscoveryRequestDto;
import com.networkmonitor.dto.DiscoveryResponseDto;
import com.networkmonitor.dto.LocalNetworkDto;
import com.networkmonitor.dto.NmapScanResultDto;
import com.networkmonitor.monitoring.NmapService;
import com.networkmonitor.service.AutoDiscoveryService;
import com.networkmonitor.service.DiscoveryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final DiscoveryService discoveryService;
    private final AutoDiscoveryService autoDiscoveryService;
    private final NmapService nmapService;

    public DiscoveryController(
            DiscoveryService discoveryService,
            AutoDiscoveryService autoDiscoveryService,
            NmapService nmapService) {
        this.discoveryService = discoveryService;
        this.autoDiscoveryService = autoDiscoveryService;
        this.nmapService = nmapService;
    }

    /** Detect the IPv4 networks currently attached to this server. */
    @GetMapping("/local-networks")
    public ResponseEntity<List<LocalNetworkDto>> getLocalNetworks() {
        return ResponseEntity.ok(autoDiscoveryService.getLocalNetworks());
    }

    /** Scan all suitable local networks and automatically import newly discovered devices. */
    @PostMapping("/auto")
    public ResponseEntity<AutoDiscoveryResponseDto> autoDiscover() {
        return ResponseEntity.ok(autoDiscoveryService.discoverAndImport());
    }

    @GetMapping("/auto/status")
    public ResponseEntity<Map<String, Object>> getAutoDiscoveryStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("running", autoDiscoveryService.isScanRunning());
        status.put("schedule", "automatic");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/scan")
    public ResponseEntity<DiscoveryResponseDto> scanSubnet(@Valid @RequestBody DiscoveryRequestDto request) {
        DiscoveryResponseDto response = discoveryService.scanSubnet(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/import")
    public ResponseEntity<List<DeviceResponseDto>> importDevices(@Valid @RequestBody List<DeviceRequestDto> devicesToImport) {
        List<DeviceResponseDto> imported = discoveryService.importDiscoveredDevices(devicesToImport);
        return new ResponseEntity<>(imported, HttpStatus.CREATED);
    }

    @GetMapping("/nmap/status")
    public ResponseEntity<Map<String, Object>> getNmapStatus() {
        Map<String, Object> status = new HashMap<>();
        boolean available = nmapService.isNmapAvailable();
        status.put("available", available);
        status.put("binary", available ? "nmap" : "Not Found");
        status.put("version", available ? "detected" : "None");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/nmap")
    public ResponseEntity<NmapScanResultDto> scanNmapSubnet(@Valid @RequestBody DiscoveryRequestDto request) {
        NmapScanResultDto response = nmapService.scanTarget(request.getSubnetCidr(), request.getStrategy());
        return ResponseEntity.ok(response);
    }
}
