package com.networkmonitor.service;

import com.networkmonitor.entity.Device;
import com.networkmonitor.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MonitoringSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringSchedulerService.class);

    private final DeviceRepository deviceRepository;
    private final DeviceMonitoringService monitoringService;
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final AtomicBoolean isCycleRunning = new AtomicBoolean(false);
    private final ExecutorService workerPool = Executors.newFixedThreadPool(10);

    private LocalDateTime lastRunTime;
    private long lastCycleDurationMs = 0;
    private int lastDevicesScannedCount = 0;

    public MonitoringSchedulerService(DeviceRepository deviceRepository, DeviceMonitoringService monitoringService) {
        this.deviceRepository = deviceRepository;
        this.monitoringService = monitoringService;
    }

    @Scheduled(fixedDelayString = "${monitoring.scheduler.interval:10000}")
    public void executeScheduledMonitoringCycle() {
        if (!active.get()) {
            return;
        }

        // Prevent overlapping execution cycles
        if (!isCycleRunning.compareAndSet(false, true)) {
            log.warn("Previous monitoring cycle is still executing. Skipping current cycle.");
            return;
        }

        try {
            long startTime = System.currentTimeMillis();
            List<Device> activeDevices = deviceRepository.findByMonitoringEnabledTrue();

            if (activeDevices.isEmpty()) {
                return;
            }

            lastRunTime = LocalDateTime.now();
            lastDevicesScannedCount = activeDevices.size();

            CountDownLatch latch = new CountDownLatch(activeDevices.size());

            for (Device device : activeDevices) {
                workerPool.submit(() -> {
                    try {
                        monitoringService.performPingCheck(device.getId());
                    } catch (Exception e) {
                        log.error("Error executing scheduled ping for device id: {}", device.getId(), e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                // Wait up to 8 seconds for all device tasks in the cycle to finish
                latch.await(8, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            lastCycleDurationMs = System.currentTimeMillis() - startTime;
        } finally {
            isCycleRunning.set(false);
        }
    }

    public boolean startScheduler() {
        active.set(true);
        return true;
    }

    public boolean stopScheduler() {
        active.set(false);
        return true;
    }

    public boolean isSchedulerActive() {
        return active.get();
    }

    public boolean isCycleRunning() {
        return isCycleRunning.get();
    }

    public LocalDateTime getLastRunTime() {
        return lastRunTime;
    }

    public long getLastCycleDurationMs() {
        return lastCycleDurationMs;
    }

    public int getLastDevicesScannedCount() {
        return lastDevicesScannedCount;
    }

    public int getWorkerPoolSize() {
        return 10;
    }
}
