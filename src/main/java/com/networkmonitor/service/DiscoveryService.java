package com.networkmonitor.service;

import com.networkmonitor.discovery.DiscoveryStrategy;
import com.networkmonitor.discovery.SubnetCalculator;
import com.networkmonitor.dto.*;
import com.networkmonitor.entity.DeviceType;
import com.networkmonitor.monitoring.PingResult;
import com.networkmonitor.monitoring.PingService;
import com.networkmonitor.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

@Service
public class DiscoveryService {
    private final Map<String, DiscoveryStrategy> strategyMap;
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    private final PingService pingService;
    private final NetworkIdentityService identityService;

    public DiscoveryService(Map<String, DiscoveryStrategy> strategyMap, DeviceRepository deviceRepository,
                            DeviceService deviceService, PingService pingService, NetworkIdentityService identityService) {
        this.strategyMap=strategyMap; this.deviceRepository=deviceRepository; this.deviceService=deviceService;
        this.pingService=pingService; this.identityService=identityService;
    }

    public DiscoveryResponseDto scanSubnet(DiscoveryRequestDto request) {
        long start=System.currentTimeMillis();
        List<String> targets=SubnetCalculator.getIpAddressesInCidr(request.getSubnetCidr());
        String key="TCP".equalsIgnoreCase(request.getStrategy()) ? "TCP_DISCOVERY" : "PING_DISCOVERY";
        DiscoveryStrategy selected=strategyMap.get(key); if(selected==null) selected=strategyMap.get("PING_DISCOVERY");
        final DiscoveryStrategy active=selected;
        int threads=Math.min(Math.max(request.getThreads()!=null?request.getThreads():20,1),50);
        int timeout=request.getTimeoutMs()!=null?Math.min(Math.max(request.getTimeoutMs(),100),10000):800;
        ExecutorService executor=Executors.newFixedThreadPool(threads);
        List<DiscoveredDeviceDto> found=Collections.synchronizedList(new ArrayList<>());
        for(String ip:targets) executor.submit(()->{
            PingResult ping=null;
            boolean reachable;
            if("PING".equalsIgnoreCase(request.getStrategy()) || request.getStrategy()==null) {
                ping=pingService.ping(ip,1,Math.max(1,(timeout+999)/1000)); reachable=ping.isReachable();
            } else reachable=active!=null && active.checkReachability(ip,timeout);
            if(!reachable) return;
            String hostname=resolveHostname(ip);
            String suggested=(hostname!=null&&!hostname.equals(ip))?hostname:"Discovered Host "+ip;
            DeviceType type=guessDeviceType(ip,hostname);
            NetworkIdentityService.Identity identity=identityService.lookup(ip);
            DiscoveredDeviceDto d=new DiscoveredDeviceDto(ip,hostname,true,suggested,type,deviceRepository.existsByIpAddress(ip));
            if(ping!=null){d.setLatencyMs(ping.getLatencyMs()); d.setPacketLossPercent(ping.getPacketLossPercent());}
            d.setMacAddress(identity.getMacAddress()); d.setVendor(identity.getVendor());
            d.setOsClue(guessOsClue(hostname));
            found.add(d);
        });
        executor.shutdown();
        try{executor.awaitTermination(5,TimeUnit.MINUTES);}catch(InterruptedException e){Thread.currentThread().interrupt();}
        int existing=0; for(DiscoveredDeviceDto d:found) if(d.isAlreadyMonitored()) existing++;
        DiscoveryResponseDto r=new DiscoveryResponseDto(); r.setSubnetCidr(request.getSubnetCidr()); r.setTotalScanned(targets.size());
        r.setDevicesDiscoveredCount(found.size()); r.setExistingDevicesCount(existing); r.setNewDevicesCount(found.size()-existing);
        r.setScanDurationMs(System.currentTimeMillis()-start); r.setDiscoveredDevices(found); return r;
    }

    public List<DeviceResponseDto> importDiscoveredDevices(List<DeviceRequestDto> devices){
        List<DeviceResponseDto> result=new ArrayList<>(); for(DeviceRequestDto d:devices) if(!deviceRepository.existsByIpAddress(d.getIpAddress())) result.add(deviceService.createDevice(d)); return result;
    }

    public DeviceResponseDto upsertDiscoveredDevice(DiscoveredDeviceDto d){
        return deviceService.upsertDiscoveredDevice(d);
    }

    private String resolveHostname(String ip){try{return InetAddress.getByName(ip).getHostName();}catch(Exception e){return ip;}}
    private DeviceType guessDeviceType(String ip,String hostname){
        if(ip.endsWith(".1")||ip.endsWith(".254")) return DeviceType.ROUTER;
        if(hostname!=null){String h=hostname.toLowerCase(Locale.ROOT); if(h.contains("router")||h.contains("gateway")||h.contains("gw"))return DeviceType.ROUTER;
            if(h.contains("switch")||h.matches(".*\\bsw[-_].*"))return DeviceType.SWITCH; if(h.contains("printer")||h.contains("print"))return DeviceType.PRINTER;
            if(h.contains("server")||h.contains("srv"))return DeviceType.SERVER;}
        return DeviceType.WORKSTATION;
    }
    private String guessOsClue(String hostname){
        if(hostname==null)return null; String h=hostname.toLowerCase(Locale.ROOT);
        if(h.contains("android"))return "Android (hostname clue)"; if(h.contains("iphone")||h.contains("ipad")||h.contains("macbook"))return "Apple OS (hostname clue)";
        if(h.contains("windows")||h.contains("win-"))return "Windows (hostname clue)"; if(h.contains("linux")||h.contains("ubuntu")||h.contains("rasp"))return "Linux (hostname clue)";
        return null;
    }
}
