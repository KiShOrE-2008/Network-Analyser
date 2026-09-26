package com.networkmonitor.controller;

import com.networkmonitor.dto.InterfaceMetricDto;
import com.networkmonitor.service.InterfaceMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class InterfaceMetricController {

    private final InterfaceMonitoringService interfaceService;

    public InterfaceMetricController(InterfaceMonitoringService interfaceService) {
        this.interfaceService = interfaceService;
    }

    @GetMapping("/{id}/interfaces")
    public ResponseEntity<List<InterfaceMetricDto>> getInterfaces(@PathVariable Long id) {
        return ResponseEntity.ok(interfaceService.getDeviceInterfaces(id));
    }

    @PostMapping("/{id}/interface-check")
    public ResponseEntity<List<InterfaceMetricDto>> checkInterfaces(@PathVariable Long id) {
        return ResponseEntity.ok(interfaceService.getDeviceInterfaces(id));
    }
}
