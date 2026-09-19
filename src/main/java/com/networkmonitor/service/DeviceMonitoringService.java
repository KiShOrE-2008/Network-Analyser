package com.networkmonitor.service;

import com.networkmonitor.dto.*;
import com.networkmonitor.entity.*;
import com.networkmonitor.monitoring.*;
import com.networkmonitor.repository.*;
import com.networkmonitor.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceMonitoringService {
    private final DeviceRepository devices;
    private final MonitoringMetricRepository metrics;
    private final PortStatusRepository ports;
    private final DeviceEventRepository events;
    private final PingService ping;
    private final PortScannerService scanner;
    private final HealthAnalyzerService health;
    private final AlertService alerts;
    private final WebSocketNotificationService ws;

    public DeviceMonitoringService(DeviceRepository d, MonitoringMetricRepository m, PortStatusRepository p,
                                   DeviceEventRepository e, PingService ps, PortScannerService s,
                                   HealthAnalyzerService h, AlertService a, WebSocketNotificationService w) {
        devices=d; metrics=m; ports=p; events=e; ping=ps; scanner=s; health=h; alerts=a; ws=w;
    }

    @Transactional
    public PingCheckResponseDto performPingCheck(Long id) {
        Device d=devices.findById(id).orElseThrow(()->new ResourceNotFoundException("Device not found with id: "+id));
        DeviceStatus oldStatus=d.getStatus();
        HealthStatus oldHealth=d.getHealthStatus();
        PingResult r=ping.ping(d.getIpAddress());
        DeviceStatus newStatus=r.isReachable()?DeviceStatus.ONLINE:DeviceStatus.OFFLINE;
        HealthStatus newHealth=health.evaluateHealth(r);
        d.setStatus(newStatus);
        d.setHealthStatus(newHealth);
        if(r.isReachable()) d.setLastSeenAt(r.getTimestamp());
        devices.save(d);

        if(oldStatus!=newStatus) event(d,"STATUS_CHANGE",oldStatus,newStatus,"Device status changed from "+oldStatus+" to "+newStatus);
        if(oldHealth!=newHealth) event(d,"HEALTH_CHANGE",oldHealth,newHealth,"Health changed from "+oldHealth+" to "+newHealth);
        alerts.processStateTransition(d,oldHealth,newHealth,r);
        metrics.save(new MonitoringMetric(d,r.getTimestamp(),r.isReachable(),r.getLatencyMs(),r.getPacketLossPercent()));

        PingCheckResponseDto x=new PingCheckResponseDto();
        x.setDeviceId(d.getId()); x.setDeviceName(d.getName()); x.setIpAddress(d.getIpAddress());
        x.setReachable(r.isReachable()); x.setLatencyMs(r.getLatencyMs()); x.setPacketLossPercent(r.getPacketLossPercent());
        x.setDeviceStatus(d.getStatus()); x.setCheckedAt(r.getTimestamp());
        ws.notifyMetricUpdate(x); ws.notifyDeviceUpdate(DeviceResponseDto.fromEntity(d));
        return x;
    }

    private void event(Device d,String t,Enum<?> oldS,Enum<?> newS,String msg) {
        events.save(new DeviceEvent(d,t,oldS==null?null:oldS.name(),newS==null?null:newS.name(),msg));
    }

    @Transactional(readOnly=true)
    public List<MetricResponseDto> getDeviceMetrics(Long id) {
        if(!devices.existsById(id)) throw new ResourceNotFoundException("Device not found with id: "+id);
        return metrics.findTop50ByDeviceIdOrderByTimestampDesc(id).stream().map(MetricResponseDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public PortScanResponseDto performPortScan(Long id) {
        Device d=devices.findById(id).orElseThrow(()->new ResourceNotFoundException("Device not found with id: "+id));
        LocalDateTime at=LocalDateTime.now();

        Set<Integer> previousOpenPorts = ports.findByDeviceId(id).stream()
            .filter(p -> p.getStatus() == PortState.OPEN)
            .map(PortStatus::getPort)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(TreeSet::new));

        List<PortStatus> out=new ArrayList<>();
        int open=0;
        ports.deleteByDeviceId(id);

        for(Map.Entry<Integer,String> e:PortScannerService.COMMON_PORTS.entrySet()) {
            PortScannerService.PortScanResult r=scanner.scanPort(d.getIpAddress(),e.getKey(),e.getValue(),400);
            out.add(ports.save(new PortStatus(d,e.getKey(),"TCP",e.getValue(),r.getState(),r.getLatencyMs(),at)));
            if(r.getState()==PortState.OPEN) open++;
        }

        Set<Integer> currentOpenPorts = out.stream()
            .filter(p -> p.getStatus() == PortState.OPEN)
            .map(PortStatus::getPort)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(TreeSet::new));

        Set<Integer> opened = new TreeSet<>(currentOpenPorts);
        opened.removeAll(previousOpenPorts);
        Set<Integer> closed = new TreeSet<>(previousOpenPorts);
        closed.removeAll(currentOpenPorts);

        if(!opened.isEmpty() || !closed.isEmpty()) {
            List<String> changes = new ArrayList<>();
            if(!opened.isEmpty()) changes.add("opened " + opened);
            if(!closed.isEmpty()) changes.add("closed " + closed);
            events.save(new DeviceEvent(d,"PORT_CHANGE",null,d.getStatus().name(),
                "Port exposure changed: " + String.join(", ", changes)));
        }

        PortScanResponseDto x=new PortScanResponseDto();
        x.setDeviceId(d.getId()); x.setDeviceName(d.getName()); x.setIpAddress(d.getIpAddress());
        x.setTotalScanned(PortScannerService.COMMON_PORTS.size()); x.setOpenPortsCount(open); x.setScannedAt(at);
        x.setPorts(out.stream().map(PortStatusDto::fromEntity).collect(Collectors.toList()));
        return x;
    }

    @Transactional(readOnly=true)
    public List<PortStatusDto> getDevicePorts(Long id) {
        if(!devices.existsById(id)) throw new ResourceNotFoundException("Device not found with id: "+id);
        return ports.findByDeviceId(id).stream().map(PortStatusDto::fromEntity).collect(Collectors.toList());
    }
}
