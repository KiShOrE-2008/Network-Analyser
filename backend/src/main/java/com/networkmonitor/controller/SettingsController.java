package com.networkmonitor.controller;

import com.networkmonitor.dto.SystemSettingsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final AtomicReference<SystemSettingsDto> currentSettings =
            new AtomicReference<>(new SystemSettingsDto(10, 60, "public", true));

    @GetMapping
    public ResponseEntity<SystemSettingsDto> getSettings() {
        return ResponseEntity.ok(currentSettings.get());
    }

    @PostMapping
    public ResponseEntity<SystemSettingsDto> updateSettings(@RequestBody SystemSettingsDto updated) {
        if (updated != null) {
            if (updated.getScanInterval() < 1) updated.setScanInterval(10);
            if (updated.getDiscoveryInterval() < 1) updated.setDiscoveryInterval(60);
            if (updated.getSnmpCommunity() == null || updated.getSnmpCommunity().isBlank()) {
                updated.setSnmpCommunity("public");
            }
            currentSettings.set(updated);
        }
        return ResponseEntity.ok(currentSettings.get());
    }
}
