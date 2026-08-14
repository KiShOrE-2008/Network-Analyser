package com.networkmonitor.discovery;

import com.networkmonitor.monitoring.PingResult;
import com.networkmonitor.monitoring.PingService;
import org.springframework.stereotype.Component;

@Component("PING_DISCOVERY")
public class PingDiscoveryStrategy implements DiscoveryStrategy {

    private final PingService pingService;

    public PingDiscoveryStrategy(PingService pingService) {
        this.pingService = pingService;
    }

    @Override
    public boolean checkReachability(String ipAddress, int timeoutMs) {
        int timeoutSeconds = Math.max(1, timeoutMs / 1000);
        PingResult result = pingService.ping(ipAddress, 1, timeoutSeconds);
        return result.isReachable();
    }

    @Override
    public String getStrategyName() {
        return "PING";
    }
}
