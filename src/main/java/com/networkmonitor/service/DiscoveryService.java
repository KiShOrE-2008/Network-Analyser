package com.networkmonitor.service;

import com.networkmonitor.discovery.DiscoveryStrategy;
import com.networkmonitor.discovery.SubnetCalculator;
import com.networkmonitor.dto.DiscoveredDeviceDto;
import com.networkmonitor.dto.DiscoveryRequestDto;
import com.networkmonitor.dto.DiscoveryResponseDto;
import com.networkmonitor.dto.DeviceRequestDto;
import com.networkmonitor.dto.DeviceResponseDto;
import com.networkmonitor.entity.DeviceType;
import com.networkmonitor.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class DiscoveryService {

    private final Map<String, DiscoveryStrategy> strategyMap;
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;

    public DiscoveryService(Map<String, DiscoveryStrategy> strategyMap, DeviceRepository deviceRepository, DeviceService deviceService) {
        this.strategyMap = strategyMap;
        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
    }

    public DiscoveryResponseDto scanSubnet(DiscoveryRequestDto request) {
        long startTime = System.currentTimeMillis();

        List<String> targetIps = SubnetCalculator.getIpAddressesInCidr(request.getSubnetCidr());

        String strategyKey = request.getStrategy() != null && request.getStrategy().equalsIgnoreCase("TCP")
                ? "TCP_DISCOVERY" : "PING_DISCOVERY";

        DiscoveryStrategy selectedStrategy = strategyMap.get(strategyKey);
        final DiscoveryStrategy activeStrategy = (selectedStrategy != null)
                ? selectedStrategy
                : strategyMap.get("PING_DISCOVERY");

        int threadCount = request.getThreads() != null ? Math.min(Math.max(request.getThreads(), 1), 50) : 20;
        int timeoutMs = request.getTimeoutMs() != null ? request.getTimeoutMs() : 800;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<DiscoveredDeviceDto> discoveredDevices = Collections.synchronizedList(new ArrayList<>());

        for (String ip : targetIps) {
            executor.submit(() -> {
                boolean reachable = activeStrategy != null && activeStrategy.checkReachability(ip, timeoutMs);
                if (reachable) {
                    boolean alreadyMonitored = deviceRepository.existsByIpAddress(ip);
                    String hostname = resolveHostname(ip);
                    String suggestedName = (hostname != null && !hostname.equals(ip)) ? hostname : "Discovered Host " + ip;
                    DeviceType suggestedType = guessDeviceType(ip, hostname);

                    discoveredDevices.add(new DiscoveredDeviceDto(
                            ip,
                            hostname,
                            true,
                            suggestedName,
                            suggestedType,
                            alreadyMonitored
                    ));
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();

        int existingCount = 0;
        int newCount = 0;
        for (DiscoveredDeviceDto d : discoveredDevices) {
            if (d.isAlreadyMonitored()) {
                existingCount++;
            } else {
                newCount++;
            }
        }

        DiscoveryResponseDto response = new DiscoveryResponseDto();
        response.setSubnetCidr(request.getSubnetCidr());
        response.setTotalScanned(targetIps.size());
        response.setDevicesDiscoveredCount(discoveredDevices.size());
        response.setNewDevicesCount(newCount);
        response.setExistingDevicesCount(existingCount);
        response.setScanDurationMs(endTime - startTime);
        response.setDiscoveredDevices(discoveredDevices);

        return response;
    }

    public List<DeviceResponseDto> importDiscoveredDevices(List<DeviceRequestDto> devicesToImport) {
        List<DeviceResponseDto> imported = new ArrayList<>();
        for (DeviceRequestDto dto : devicesToImport) {
            if (!deviceRepository.existsByIpAddress(dto.getIpAddress())) {
                imported.add(deviceService.createDevice(dto));
            }
        }
        return imported;
    }

    private String resolveHostname(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.getHostName();
        } catch (Exception e) {
            return ip;
        }
    }

    private DeviceType guessDeviceType(String ip, String hostname) {
        if (ip.endsWith(".1") || ip.endsWith(".254")) {
            return DeviceType.ROUTER;
        }
        if (hostname != null) {
            String lowerHost = hostname.toLowerCase();
            if (lowerHost.contains("router") || lowerHost.contains("gw") || lowerHost.contains("gateway")) {
                return DeviceType.ROUTER;
            }
            if (lowerHost.contains("switch") || lowerHost.contains("sw")) {
                return DeviceType.SWITCH;
            }
            if (lowerHost.contains("server") || lowerHost.contains("srv") || lowerHost.contains("host")) {
                return DeviceType.SERVER;
            }
            if (lowerHost.contains("printer") || lowerHost.contains("ptr")) {
                return DeviceType.PRINTER;
            }
        }
        return DeviceType.WORKSTATION;
    }
}
