package com.networkmonitor.discovery;

import com.networkmonitor.entity.PortState;
import com.networkmonitor.monitoring.PortScannerService;
import org.springframework.stereotype.Component;

@Component("TCP_DISCOVERY")
public class TcpDiscoveryStrategy implements DiscoveryStrategy {

    private final PortScannerService portScannerService;
    private static final int[] DISCOVERY_PORTS = {80, 443, 22, 445, 8080};

    public TcpDiscoveryStrategy(PortScannerService portScannerService) {
        this.portScannerService = portScannerService;
    }

    @Override
    public boolean checkReachability(String ipAddress, int timeoutMs) {
        for (int port : DISCOVERY_PORTS) {
            PortScannerService.PortScanResult result = portScannerService.scanPort(ipAddress, port, null, Math.min(timeoutMs, 300));
            if (result.getState() == PortState.OPEN) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getStrategyName() {
        return "TCP";
    }
}
