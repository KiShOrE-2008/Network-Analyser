package com.networkmonitor.monitoring;

import com.networkmonitor.dto.SnmpMetricDto;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SnmpService {

    public SnmpMetricDto querySnmpMetrics(Long deviceId, String ipAddress, String community) {
        String comm = (community != null && !community.isBlank()) ? community : "public";

        // Generate realistic hardware metrics derived deterministically from IP and deviceId
        Random random = new Random(ipAddress.hashCode() + deviceId);
        long uptime = 86400L * (5 + random.nextInt(120)) + random.nextInt(3600);
        double cpu = Math.round((15.0 + random.nextDouble() * 35.0) * 10.0) / 10.0;
        double memory = Math.round((35.0 + random.nextDouble() * 40.0) * 10.0) / 10.0;
        int interfaces = 2 + random.nextInt(8);

        return new SnmpMetricDto(deviceId, ipAddress, uptime, cpu, memory, interfaces, comm);
    }
}
