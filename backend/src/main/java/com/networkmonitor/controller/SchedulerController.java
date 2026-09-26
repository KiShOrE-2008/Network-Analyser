package com.networkmonitor.controller;

import com.networkmonitor.service.MonitoringSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    private final MonitoringSchedulerService schedulerService;

    public SchedulerController(MonitoringSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("active", schedulerService.isSchedulerActive());
        status.put("workerPoolSize", schedulerService.getWorkerPoolSize());
        status.put("lastRunTime", schedulerService.getLastRunTime());
        status.put("lastCycleDurationMs", schedulerService.getLastCycleDurationMs());
        status.put("lastDevicesScannedCount", schedulerService.getLastDevicesScannedCount());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startScheduler() {
        schedulerService.startScheduler();
        return getSchedulerStatus();
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopScheduler() {
        schedulerService.stopScheduler();
        return getSchedulerStatus();
    }
}
