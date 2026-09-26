package com.networkmonitor.service;

import com.networkmonitor.discovery.SubnetCalculator;
import com.networkmonitor.dto.AutoDiscoveryResponseDto;
import com.networkmonitor.dto.DiscoveredDeviceDto;
import com.networkmonitor.dto.DiscoveryRequestDto;
import com.networkmonitor.dto.DiscoveryResponseDto;
import com.networkmonitor.dto.LocalNetworkDto;
import com.networkmonitor.dto.NmapHostResultDto;
import com.networkmonitor.dto.NmapScanResultDto;
import com.networkmonitor.monitoring.NmapService;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AutoDiscoveryService {
    private static final Logger log = LoggerFactory.getLogger(AutoDiscoveryService.class);
    private static final int DEFAULT_TIMEOUT_MS = 800;
    private static final int DEFAULT_THREADS = 32;
    private static final int MAX_HOSTS_PER_NETWORK = 1022;

    private final DiscoveryService discoveryService;
    private final DeviceService deviceService;
    private final NmapService nmapService;
    private final AtomicBoolean scanRunning = new AtomicBoolean(false);

    public AutoDiscoveryService(DiscoveryService discoveryService, DeviceService deviceService, NmapService nmapService) {
        this.discoveryService = discoveryService;
        this.deviceService = deviceService;
        this.nmapService = nmapService;
    }

    public List<LocalNetworkDto> getLocalNetworks() {
        List<LocalNetworkDto> networks = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!isUsableInterface(ni)) continue;
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress address = ia.getAddress();
                    short prefix = ia.getNetworkPrefixLength();
                    if (!(address instanceof Inet4Address) || prefix < 16 || prefix > 32) continue;
                    String cidr = networkCidr(address, prefix);
                    if (cidr != null && seen.add(cidr)) {
                        networks.add(new LocalNetworkDto(ni.getName(), address.getHostAddress(), prefix, cidr));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Unable to enumerate local network interfaces: {}", e.getMessage());
        }
        return networks;
    }

    public AutoDiscoveryResponseDto discoverAndImport() {
        if (!scanRunning.compareAndSet(false, true)) throw new IllegalStateException("A network discovery scan is already running");
        long start = System.currentTimeMillis();
        AutoDiscoveryResponseDto response = new AutoDiscoveryResponseDto();
        try {
            List<LocalNetworkDto> networks = getLocalNetworks();
            response.setNetworks(networks);
            response.setNetworksScanned(networks.size());
            List<DiscoveredDeviceDto> all = new ArrayList<>();
            Set<String> seenIps = new HashSet<>();
            int hostsScanned = 0, imported = 0;

            for (LocalNetworkDto network : networks) {
                List<String> hosts = SubnetCalculator.getIpAddressesInCidr(network.getCidr());
                if (hosts.size() > MAX_HOSTS_PER_NETWORK) {
                    log.info("Skipping {} because it contains {} hosts", network.getCidr(), hosts.size());
                    continue;
                }
                hostsScanned += hosts.size();

                DiscoveryRequestDto request = new DiscoveryRequestDto();
                request.setSubnetCidr(network.getCidr());
                request.setStrategy("PING");
                request.setTimeoutMs(DEFAULT_TIMEOUT_MS);
                request.setThreads(DEFAULT_THREADS);
                DiscoveryResponseDto ping = discoveryService.scanSubnet(request);

                Map<String, NmapHostResultDto> nmapHosts = new HashMap<>();
                if (nmapService.isNmapAvailable()) {
                    try {
                        NmapScanResultDto nmap = nmapService.scanTarget(network.getCidr(), "DETAILED");
                        for (NmapHostResultDto host : nmap.getHosts()) nmapHosts.put(host.getIpAddress(), host);
                    } catch (Exception e) {
                        log.debug("Nmap enrichment failed for {}: {}", network.getCidr(), e.getMessage());
                    }
                }

                for (DiscoveredDeviceDto device : ping.getDiscoveredDevices()) {
                    if (!seenIps.add(device.getIpAddress())) continue;
                    NmapHostResultDto host = nmapHosts.get(device.getIpAddress());
                    if (host != null) {
                        if (host.getHostname() != null && !host.getHostname().equals(device.getIpAddress())) device.setHostname(host.getHostname());
                        device.setMacAddress(host.getMacAddress());
                        device.setVendor(host.getVendor());
                        if (host.getOsMatch() != null && !host.getOsMatch().equalsIgnoreCase("Unknown OS")) device.setOsClue(host.getOsMatch());
                        device.setSuggestedName(device.getHostname() != null ? device.getHostname() : device.getSuggestedName());
                    }
                    all.add(device);
                    boolean wasExisting = device.isAlreadyMonitored();
                    deviceService.upsertDiscoveredDevice(device);
                    if (!wasExisting) imported++;
                }
            }

            response.setDevices(all);
            response.setHostsScanned(hostsScanned);
            response.setDevicesDiscovered(all.size());
            response.setNewDevicesImported(imported);
            response.setDurationMs(System.currentTimeMillis() - start);
            return response;
        } finally {
            scanRunning.set(false);
        }
    }

    @Scheduled(initialDelayString = "${discovery.scheduler.initial-delay:10000}", fixedDelayString = "${discovery.scheduler.interval:60000}")
    public void scheduledDiscovery() {
        try {
            AutoDiscoveryResponseDto result = discoverAndImport();
            log.info("Automatic discovery completed: {} device(s), {} new import(s)", result.getDevicesDiscovered(), result.getNewDevicesImported());
        } catch (Exception e) {
            log.warn("Automatic discovery failed: {}", e.getMessage());
        }
    }

    public boolean isScanRunning() { return scanRunning.get(); }

    private boolean isUsableInterface(NetworkInterface ni) {
        try {
            String name = ni.getName().toLowerCase();
            return ni.isUp() && !ni.isLoopback() && !name.startsWith("docker") && !name.startsWith("br-") && !name.startsWith("veth") && !name.startsWith("virbr");
        } catch (Exception e) { return false; }
    }

    private String networkCidr(InetAddress address, short prefix) {
        try {
            byte[] b = address.getAddress();
            int ip = ((b[0] & 255) << 24) | ((b[1] & 255) << 16) | ((b[2] & 255) << 8) | (b[3] & 255);
            int mask = prefix == 0 ? 0 : (int)(0xffffffffL << (32 - prefix));
            int network = ip & mask;
            return ((network >>> 24) & 255) + "." + ((network >>> 16) & 255) + "." + ((network >>> 8) & 255) + "." + (network & 255) + "/" + prefix;
        } catch (Exception e) { return null; }
    }
}
