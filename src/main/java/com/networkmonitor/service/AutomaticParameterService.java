package com.networkmonitor.service;

import com.networkmonitor.entity.Device;
import com.networkmonitor.monitoring.SnmpService;
import com.networkmonitor.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Automatically collects real device parameters after discovery.
 * Ping is available for ordinary hosts; SNMP hardware values are collected
 * when the target exposes SNMP with the configured community.
 */
@Service
public class AutomaticParameterService {
    private static final Logger log = LoggerFactory.getLogger(AutomaticParameterService.class);

    private final DeviceRepository deviceRepository;
    private final DeviceMonitoringService monitoringService;
    private final SnmpService snmpService;

    public AutomaticParameterService(DeviceRepository deviceRepository,
                                     DeviceMonitoringService monitoringService,
                                     SnmpService snmpService) {
        this.deviceRepository = deviceRepository;
        this.monitoringService = monitoringService;
        this.snmpService = snmpService;
    }

    @Scheduled(
        initialDelayString = "${parameters.scheduler.initial-delay:20000}",
        fixedDelayString = "${parameters.scheduler.interval:30000}"
    )
    public void collectParameters() {
        int checked = 0;
        for (Device device : deviceRepository.findAll()) {
            if (!device.isMonitoringEnabled()) continue;
            try {
                monitoringService.performPingCheck(device.getId());
                checked++;
            } catch (Exception e) {
                log.debug("Ping collection failed for {}: {}", device.getIpAddress(), e.getMessage());
            }
            try {
                snmpService.querySnmpMetrics(device.getId(), device.getIpAddress(), "public");
            } catch (Exception e) {
                // SNMP is optional. Do not mark a reachable device offline because SNMP is unavailable.
                log.debug("SNMP unavailable for {}: {}", device.getIpAddress(), e.getMessage());
            }
        }
        if (checked > 0) log.debug("Automatic parameter collection checked {} device(s)", checked);
    }
}
