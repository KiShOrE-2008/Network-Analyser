package com.networkmonitor.discovery;

import com.networkmonitor.dto.NmapHostResultDto;
import com.networkmonitor.dto.NmapScanResultDto;
import com.networkmonitor.monitoring.NmapService;
import org.springframework.stereotype.Component;

@Component("NMAP_DISCOVERY")
public class NmapDiscoveryStrategy implements DiscoveryStrategy {

    private final NmapService nmapService;

    public NmapDiscoveryStrategy(NmapService nmapService) {
        this.nmapService = nmapService;
    }

    @Override
    public boolean checkReachability(String ipAddress, int timeoutMs) {
        if (!nmapService.isNmapAvailable()) {
            return false;
        }

        NmapScanResultDto scanResult = nmapService.scanTarget(ipAddress, "HOST_DISCOVERY");
        if (scanResult.getHosts() != null && !scanResult.getHosts().isEmpty()) {
            NmapHostResultDto host = scanResult.getHosts().get(0);
            return "up".equalsIgnoreCase(host.getStatus());
        }

        return false;
    }

    @Override
    public String getStrategyName() {
        return "NMAP";
    }
}
