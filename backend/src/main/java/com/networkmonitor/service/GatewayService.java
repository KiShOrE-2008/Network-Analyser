package com.networkmonitor.service;

import com.networkmonitor.dto.GatewayStatusDto;
import com.networkmonitor.dto.LocalNetworkDto;
import com.networkmonitor.entity.GatewayMetric;
import com.networkmonitor.monitoring.JitterCalculator;
import com.networkmonitor.repository.GatewayMetricRepository;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Service
public class GatewayService {

    private final AutoDiscoveryService autoDiscoveryService;
    private final GatewayMetricRepository gatewayMetricRepository;

    public GatewayService(AutoDiscoveryService autoDiscoveryService, GatewayMetricRepository gatewayMetricRepository) {
        this.autoDiscoveryService = autoDiscoveryService;
        this.gatewayMetricRepository = gatewayMetricRepository;
    }

    public GatewayStatusDto detectAndCheckGateway() {
        List<LocalNetworkDto> localNetworks = autoDiscoveryService.getLocalNetworks();
        String localIp = "127.0.0.1";
        String subnetCidr = "127.0.0.1/32";
        String interfaceName = "lo";
        String gatewayIp = "127.0.0.1";

        if (localNetworks != null && !localNetworks.isEmpty()) {
            LocalNetworkDto primaryNet = localNetworks.get(0);
            localIp = primaryNet.getAddress();
            subnetCidr = primaryNet.getCidr();
            interfaceName = primaryNet.getInterfaceName();

            // Calculate likely gateway IP (e.g. x.x.x.1)
            int lastDot = localIp.lastIndexOf('.');
            if (lastDot > 0) {
                gatewayIp = localIp.substring(0, lastDot + 1) + "1";
            }
        }

        // Perform ping samples to gateway for latency and jitter calculation
        List<Double> latencies = new ArrayList<>();
        int reachabilityCount = 0;
        int totalProbes = 5;

        for (int i = 0; i < totalProbes; i++) {
            long start = System.nanoTime();
            try {
                InetAddress addr = InetAddress.getByName(gatewayIp);
                boolean reachable = addr.isReachable(500);
                long elapsed = System.nanoTime() - start;
                if (reachable) {
                    reachabilityCount++;
                    latencies.add(Math.round((elapsed / 1_000_000.0) * 100.0) / 100.0);
                }
            } catch (Exception ignored) {
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
}
