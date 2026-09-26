package com.networkmonitor.service;

import com.networkmonitor.dto.GatewayStatusDto;
import com.networkmonitor.dto.LocalNetworkDto;
import com.networkmonitor.entity.GatewayMetric;
import com.networkmonitor.monitoring.JitterCalculator;
import com.networkmonitor.monitoring.PingResult;
import com.networkmonitor.monitoring.PingService;
import com.networkmonitor.repository.GatewayMetricRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class GatewayService {

    private static final Logger log = LoggerFactory.getLogger(GatewayService.class);

    private final AutoDiscoveryService autoDiscoveryService;
    private final GatewayMetricRepository gatewayMetricRepository;
    private final PingService pingService;

    public GatewayService(AutoDiscoveryService autoDiscoveryService, GatewayMetricRepository gatewayMetricRepository, PingService pingService) {
        this.autoDiscoveryService = autoDiscoveryService;
        this.gatewayMetricRepository = gatewayMetricRepository;
        this.pingService = pingService;
    }

    public GatewayStatusDto detectAndCheckGateway() {
        List<LocalNetworkDto> localNetworks = autoDiscoveryService.getLocalNetworks();
        String localIp = null;
        String subnetCidr = null;
        String interfaceName = null;
        String gatewayIp = discoverDefaultGatewayIp();

        if (localNetworks != null && !localNetworks.isEmpty()) {
            LocalNetworkDto primaryNet = localNetworks.get(0);
            localIp = primaryNet.getAddress();
            subnetCidr = primaryNet.getCidr();
            interfaceName = primaryNet.getInterfaceName();

            if (gatewayIp == null && localIp != null) {
                // Heuristic backup if system routing table isn't accessible
                int lastDot = localIp.lastIndexOf('.');
                if (lastDot > 0) {
                    gatewayIp = localIp.substring(0, lastDot + 1) + "1";
                }
            }
        }

        if (gatewayIp == null) {
            log.warn("No network gateway discovered.");
            GatewayMetric emptyMetric = new GatewayMetric();
            emptyMetric.setGatewayIp("UNKNOWN");
            emptyMetric.setLocalIp(localIp);
            emptyMetric.setNetworkCidr(subnetCidr);
            emptyMetric.setInterfaceName(interfaceName);
            emptyMetric.setReachable(false);
            return GatewayStatusDto.fromEntity(emptyMetric);
        }

        // Perform ICMP ping samples using PingService for accurate latency and jitter
        List<Double> latencies = new ArrayList<>();
        int reachabilityCount = 0;
        int totalProbes = 4;

        for (int i = 0; i < totalProbes; i++) {
            PingResult pingResult = pingService.ping(gatewayIp, 1, 1);
            if (pingResult.isReachable()) {
                reachabilityCount++;
                if (pingResult.getLatencyMs() != null) {
                    latencies.add(pingResult.getLatencyMs());
                }
            }
        }

        boolean isReachable = reachabilityCount > 0;
        double avgLatency = latencies.isEmpty() ? 0.0 : Math.round((latencies.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)) * 100.0) / 100.0;
        double packetLoss = Math.round(((totalProbes - reachabilityCount) / (double) totalProbes * 100.0) * 10.0) / 10.0;
        double jitterMs = JitterCalculator.calculateJitterMs(latencies);

        GatewayMetric metric = new GatewayMetric();
        metric.setGatewayIp(gatewayIp);
        metric.setLocalIp(localIp);
        metric.setNetworkCidr(subnetCidr);
        metric.setInterfaceName(interfaceName);
        metric.setLatencyMs(isReachable ? avgLatency : null);
        metric.setPacketLossPercent(packetLoss);
        metric.setJitterMs(isReachable ? jitterMs : 0.0);
        metric.setReachable(isReachable);

        gatewayMetricRepository.save(metric);

        return GatewayStatusDto.fromEntity(metric);
    }

    private String discoverDefaultGatewayIp() {
        // Read Linux /proc/net/route for default route (Destination 00000000)
        File procRoute = new File("/proc/net/route");
        if (procRoute.exists() && procRoute.canRead()) {
            try (BufferedReader br = new BufferedReader(new FileReader(procRoute))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.trim().split("\\s+");
                    if (tokens.length >= 3 && "00000000".equals(tokens[1])) {
                        String hexGw = tokens[2];
                        if (!"00000000".equals(hexGw) && hexGw.length() == 8) {
                            int b1 = Integer.parseInt(hexGw.substring(6, 8), 16);
                            int b2 = Integer.parseInt(hexGw.substring(4, 6), 16);
                            int b3 = Integer.parseInt(hexGw.substring(2, 4), 16);
                            int b4 = Integer.parseInt(hexGw.substring(0, 2), 16);
                            return String.format("%d.%d.%d.%d", b1, b2, b3, b4);
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Could not parse /proc/net/route: {}", e.getMessage());
            }
        }
        return null;
    }
}
