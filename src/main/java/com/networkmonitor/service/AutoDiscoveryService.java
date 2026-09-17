package com.networkmonitor.service;

import com.networkmonitor.discovery.SubnetCalculator;
import com.networkmonitor.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AutoDiscoveryService {
    private static final Logger log=LoggerFactory.getLogger(AutoDiscoveryService.class);
    private static final int MAX_HOSTS_PER_NETWORK=1022;
    private final DiscoveryService discoveryService;
    private final AtomicBoolean scanRunning=new AtomicBoolean(false);
    public AutoDiscoveryService(DiscoveryService discoveryService){this.discoveryService=discoveryService;}

    public List<LocalNetworkDto> getLocalNetworks(){
        List<LocalNetworkDto> result=new ArrayList<>(); Set<String> seen=new HashSet<>();
        try{Enumeration<NetworkInterface> interfaces=NetworkInterface.getNetworkInterfaces(); while(interfaces.hasMoreElements()){
            NetworkInterface ni=interfaces.nextElement(); if(!isUsable(ni))continue;
            for(InterfaceAddress ia:ni.getInterfaceAddresses()){
                InetAddress a=ia.getAddress(); short prefix=ia.getNetworkPrefixLength(); if(!(a instanceof Inet4Address)||prefix<16||prefix>30)continue;
                String cidr=networkCidr(a,prefix); if(cidr!=null&&seen.add(cidr))result.add(new LocalNetworkDto(ni.getName(),a.getHostAddress(),prefix,cidr));
            }
        }}catch(Exception e){log.warn("Unable to enumerate local networks: {}",e.getMessage());}
        return result;
    }

    public AutoDiscoveryResponseDto discoverAndImport(){
        if(!scanRunning.compareAndSet(false,true))throw new IllegalStateException("A network discovery scan is already running");
        long start=System.currentTimeMillis(); AutoDiscoveryResponseDto r=new AutoDiscoveryResponseDto();
        try{List<LocalNetworkDto> networks=getLocalNetworks(); r.setNetworks(networks); r.setNetworksScanned(networks.size());
            List<DiscoveredDeviceDto> all=new ArrayList<>(); Set<String> ips=new HashSet<>(); int hosts=0, imported=0;
            for(LocalNetworkDto n:networks){List<String> target=SubnetCalculator.getIpAddressesInCidr(n.getCidr()); if(target.size()>MAX_HOSTS_PER_NETWORK){log.info("Skipping {} ({} hosts exceeds limit {})",n.getCidr(),target.size(),MAX_HOSTS_PER_NETWORK);continue;} hosts+=target.size();
                DiscoveryRequestDto q=new DiscoveryRequestDto();q.setSubnetCidr(n.getCidr());q.setStrategy("PING");q.setTimeoutMs(800);q.setThreads(32); DiscoveryResponseDto scan=discoveryService.scanSubnet(q);
                for(DiscoveredDeviceDto d:scan.getDiscoveredDevices())if(ips.add(d.getIpAddress())){all.add(d); if(!d.isAlreadyMonitored()){discoveryService.upsertDiscoveredDevice(d);imported++;}else discoveryService.upsertDiscoveredDevice(d);}
            }
            r.setDevices(all);r.setHostsScanned(hosts);r.setDevicesDiscovered(all.size());r.setNewDevicesImported(imported);r.setDurationMs(System.currentTimeMillis()-start);return r;
        }finally{scanRunning.set(false);}
    }
    @Scheduled(initialDelayString="${discovery.scheduler.initial-delay:10000}",fixedDelayString="${discovery.scheduler.interval:60000}")
    public void scheduledDiscovery(){try{AutoDiscoveryResponseDto r=discoverAndImport();log.info("Automatic discovery: {} found, {} new",r.getDevicesDiscovered(),r.getNewDevicesImported());}catch(Exception e){log.warn("Automatic discovery failed: {}",e.getMessage());}}
    public boolean isScanRunning(){return scanRunning.get();}
    private boolean isUsable(NetworkInterface ni){try{String n=ni.getName().toLowerCase(Locale.ROOT);return ni.isUp()&&!ni.isLoopback()&&!ni.isVirtual()&&!n.startsWith("docker")&&!n.startsWith("br-")&&!n.startsWith("veth")&&!n.startsWith("virbr");}catch(Exception e){return false;}}
    private String networkCidr(InetAddress a,short prefix){byte[] b=a.getAddress();int ip=((b[0]&255)<<24)|((b[1]&255)<<16)|((b[2]&255)<<8)|(b[3]&255);int mask=prefix==0?0:(int)(0xffffffffL<<(32-prefix));int net=ip&mask;return ((net>>>24)&255)+"."+((net>>>16)&255)+"."+((net>>>8)&255)+"."+(net&255)+"/"+prefix;}
}
