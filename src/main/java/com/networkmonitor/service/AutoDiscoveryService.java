package com.networkmonitor.service;

import com.networkmonitor.discovery.SubnetCalculator;
import com.networkmonitor.dto.AutoDiscoveryResponseDto;
import com.networkmonitor.dto.DeviceRequestDto;
import com.networkmonitor.dto.DiscoveredDeviceDto;
import com.networkmonitor.dto.DiscoveryRequestDto;
import com.networkmonitor.dto.DiscoveryResponseDto;
import com.networkmonitor.dto.LocalNetworkDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AutoDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(AutoDiscoveryService.class);
    private static final int DEFAULT_TIMEOUT_MS = 800;
    private static final int DEFAULT_THREADS = 32;
    private static final int MAX_HOSTS_PER_NETWORK = 1022;

    private final DiscoveryService discoveryService;
    private final AtomicBoolean scanRunning = new AtomicBoolean(false);

    public AutoDiscoveryService(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public List<LocalNetworkDto> getLocalNetworks() {
        List<LocalNetworkDto> networks = new ArrayList<>();
        Set<String> seenCidrs = new HashSet<>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!isUsableInterface(networkInterface)) {
                    continue;
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress address = interfaceAddress.getAddress();
                    short prefix = interfaceAddress.getNetworkPrefixLength();

                    if (!(address instanceof Inet4Address) || prefix < 16 || prefix > 32) {
                        continue;
                    }

                    String cidr = networkCidr(address, prefix);
                    if (cidr != null && seenCidrs.add(cidr)) {
                        networks.add(new LocalNetworkDto(
                                networkInterface.getName(),
                                address.getHostAddress(),
                                prefix,
                                cidr
                        ));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Unable to enumerate local network interfaces: {}", e.getMessage());
        }

        return networks;
    }

    public AutoDiscoveryResponseDto discoverAndImport() {
        if (!scanRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("A network discovery scan is already running");
        }

        long start = System.currentTimeMillis();
        AutoDiscoveryResponseDto response = new AutoDiscoveryResponseDto();

        try {
            List<LocalNetworkDto> networks = getLocalNetworks();
            response.setNetworks(networks);
            response.setNetworksScanned(networks.size());

            List<DiscoveredDeviceDto> allDevices = new ArrayList<>();
            Set<String> discoveredIps = new HashSet<>();
            int hostsScanned = 0;
            int imported = 0;

            for (LocalNetworkDto network : networks) {
                List<String> hosts = SubnetCalculator.getIpAddressesInCidr(network.getCidr());
                if (hosts.size() > MAX_HOSTS_PER_NETWORK) {
                    log.info("Skipping {} because it contains {} hosts; automatic discovery limit is {}",
                            network.getCidr(), hosts.size(), MAX_HOSTS_PER_NETWORK);
                    continue;
                }

                hostsScanned += hosts.size();

                DiscoveryRequestDto request = new DiscoveryRequestDto();
                request.setSubnetCidr(network.getCidr());
                request.setStrategy("PING");
                request.setTimeoutMs(DEFAULT_TIMEOUT_MS);
                request.setThreads(DEFAULT_THREADS);

                DiscoveryResponseDto result = discoveryService.scanSubnet(request);
                for (DiscoveredDeviceDto device : result.getDiscoveredDevices()) {
                    if (discoveredIps.add(device.getIpAddress())) {
                        allDevices.add(device);

                        if (!device.isAlreadyMonitored()) {
                            DeviceRequestDto importRequest = new DeviceRequestDto();
                            importRequest.setName(device.getSuggestedName());
                            importRequest.setIpAddress(device.getIpAddress());
                            importRequest.setHostname(device.getHostname());
                            importRequest.setDeviceType(device.getSuggestedType());
                            importRequest.setMonitoringEnabled(true);
                            importRequest.setScanInterval(10);
                            if (!discoveryService.importDiscoveredDevices(List.of(importRequest)).isEmpty()) {
                                imported++;
                            }
                        }
                    }
                }
            }

            response.setDevices(allDevices);
            response.setHostsScanned(hostsScanned);
            response.setDevicesDiscovered(allDevices.size());
            response.setNewDevicesImported(imported);
            response.setDurationMs(System.currentTimeMillis() - start);
            return response;
        } finally {
            scanRunning.set(false);
        }
    }

    @Scheduled(
            initialDelayString = "${discovery.scheduler.initial-delay:10000}",
            fixedDelayString = "${discovery.scheduler.interval:60000}"
    )
    public void scheduledDiscovery() {
        try {
            AutoDiscoveryResponseDto result = discoverAndImport();
            log.info("Automatic discovery completed: {} device(s) found, {} new device(s) imported",
                    result.getDevicesDiscovered(), result.getNewDevicesImported());
        } catch (Exception e) {
            log.warn("Automatic discovery failed: {}", e.getMessage());
        }
    }

    public boolean isScanRunning() {
        return scanRunning.get();
    }

    private boolean isUsableInterface(NetworkInterface networkInterface) {
        try {
            String name = networkInterface.getName().toLowerCase();
            return networkInterface.isUp()
                    && !networkInterface.isLoopback()
                    && !name.startsWith("docker")
                    && !name.startsWith("br-")
                    && !name.startsWith("veth")
                    && !name.startsWith("virbr");
        } catch (Exception e) {
            return false;
        }
    }

    private String networkCidr(InetAddress address, short prefix) {
        try {
            byte[] bytes = address.getAddress();
            int ip = ((bytes[0] & 0xff) << 24)
                    | ((bytes[1] & 0xff) << 16)
                    | ((bytes[2] & 0xff) << 8)
                    | (bytes[3] & 0xff);
            int mask = prefix == 0 ? 0 : (int) (0xffffffffL << (32 - prefix));
            int network = ip & mask;
            return ((network >>> 24) & 255) + "."
                    + ((network >>> 16) & 255) + "."
                    + ((network >>> 8) & 255) + "."
                    + (network & 255) + "/" + prefix;
        } catch (Exception e) {
            return null;
        }
    }
}
