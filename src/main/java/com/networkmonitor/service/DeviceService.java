package com.networkmonitor.service;

import com.networkmonitor.entity.*;
import com.networkmonitor.dto.*;
import com.networkmonitor.exception.ResourceNotFoundException;
import com.networkmonitor.repository.DeviceEventRepository;
import com.networkmonitor.repository.DeviceRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private final DeviceRepository repo;
    private final DeviceEventRepository events;

    public DeviceService(DeviceRepository r, DeviceEventRepository e) {
        repo = r;
        events = e;
    }

    @Transactional(readOnly = true)
    public List<DeviceResponseDto> getAllDevices(String search, DeviceType type, DeviceStatus status) {
        return repo.findAll((root, q, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (type != null) p.add(cb.equal(root.get("deviceType"), type));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (search != null && !search.trim().isEmpty()) {
                String x = "%" + search.trim().toLowerCase() + "%";
                p.add(cb.or(
                    cb.like(cb.lower(root.get("name")), x),
                    cb.like(root.get("ipAddress"), x),
                    cb.like(cb.lower(root.get("hostname")), x),
                    cb.like(cb.lower(root.get("vendor")), x),
                    cb.like(cb.lower(root.get("macAddress")), x)
                ));
            }
            return cb.and(p.toArray(new Predicate[0]));
        }).stream().map(DeviceResponseDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeviceResponseDto getDeviceById(Long id) {
        return DeviceResponseDto.fromEntity(repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id)));
    }

    @Transactional
    public DeviceResponseDto createDevice(DeviceRequestDto x) {
        validate(x);
        if (repo.existsByIpAddress(x.getIpAddress()))
            throw new IllegalArgumentException("Device with IP address '" + x.getIpAddress() + "' already exists.");
        Device d = new Device();
        map(x, d);
        return DeviceResponseDto.fromEntity(repo.save(d));
    }

    @Transactional
    public DeviceResponseDto updateDevice(Long id, DeviceRequestDto x) {
        validate(x);
        Device d = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
        if (!d.getIpAddress().equalsIgnoreCase(x.getIpAddress()) && repo.existsByIpAddress(x.getIpAddress()))
            throw new IllegalArgumentException("Device with IP address '" + x.getIpAddress() + "' already exists.");
        map(x, d);
        return DeviceResponseDto.fromEntity(repo.save(d));
    }

    @Transactional
    public DeviceResponseDto upsertDiscoveredDevice(DiscoveredDeviceDto x) {
        Device d = repo.findByIpAddress(x.getIpAddress()).orElse(null);
        boolean isNew = d == null;

        if (isNew) {
            d = new Device();
            d.setIpAddress(x.getIpAddress());
            d.setName(x.getSuggestedName() != null ? x.getSuggestedName() : x.getIpAddress());
            d.setDeviceType(x.getSuggestedType() != null ? x.getSuggestedType() : DeviceType.WORKSTATION);
            d.setMonitoringEnabled(true);
            d.setScanInterval(10);
        }

        String oldHostname = d.getHostname();
        String oldMac = d.getMacAddress();
        String oldVendor = d.getVendor();
        String oldOs = d.getOsClue();
        DeviceType oldType = d.getDeviceType();

        if (x.getHostname() != null && !x.getHostname().equals(x.getIpAddress()))
            d.setHostname(x.getHostname());
        if (x.getSuggestedType() != null) d.setDeviceType(x.getSuggestedType());
        if (x.getMacAddress() != null) d.setMacAddress(x.getMacAddress());
        if (x.getVendor() != null && !x.getVendor().equals("Unknown")) d.setVendor(x.getVendor());
        if (x.getOsClue() != null) d.setOsClue(x.getOsClue());

        d.setStatus(DeviceStatus.ONLINE);
        d.setLastSeenAt(LocalDateTime.now());
        Device saved = repo.save(d);

        if (isNew) {
            events.save(new DeviceEvent(saved, "DEVICE_DISCOVERED", null, DeviceStatus.ONLINE.name(),
                "New device discovered at " + saved.getIpAddress()));
        } else {
            List<String> changes = new ArrayList<>();
            addChange(changes, "MAC", oldMac, saved.getMacAddress());
            addChange(changes, "hostname", oldHostname, saved.getHostname());
            addChange(changes, "vendor", oldVendor, saved.getVendor());
            addChange(changes, "OS clue", oldOs, saved.getOsClue());
            if (oldType != saved.getDeviceType())
                changes.add("device type: " + value(oldType) + " -> " + value(saved.getDeviceType()));

            if (!changes.isEmpty()) {
                events.save(new DeviceEvent(saved, "NETWORK_CHANGE", null, DeviceStatus.ONLINE.name(),
                    "Network identity changed: " + String.join("; ", changes)));
            }
        }

        return DeviceResponseDto.fromEntity(saved);
    }

    private void addChange(List<String> changes, String field, String oldValue, String newValue) {
        if (!same(oldValue, newValue) && newValue != null && !newValue.isBlank())
            changes.add(field + ": " + value(oldValue) + " -> " + newValue);
    }

    private boolean same(String a, String b) {
        return Objects.equals(normalize(a), normalize(b));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String value(Object value) {
        return value == null ? "unknown" : String.valueOf(value);
    }

    @Transactional
    public DeviceResponseDto toggleMonitoring(Long id) {
        Device d = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
        d.setMonitoringEnabled(!d.isMonitoringEnabled());
        return DeviceResponseDto.fromEntity(repo.save(d));
    }

    @Transactional
    public void deleteDevice(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Device not found with id: " + id);
        repo.deleteById(id);
    }

    private void validate(DeviceRequestDto x) {
        if (x.getName() == null || x.getName().trim().isEmpty())
            throw new IllegalArgumentException("Device name cannot be empty.");
        if (x.getIpAddress() == null || !validIp(x.getIpAddress()))
            throw new IllegalArgumentException("Invalid IP address: " + x.getIpAddress());
        if (x.getDeviceType() == null)
            throw new IllegalArgumentException("Device type is required.");
    }

    private boolean validIp(String ip) {
        try {
            String[] p = ip.split("\\.");
            if (p.length != 4) return false;
            for (String s : p) {
                int v = Integer.parseInt(s);
                if (v < 0 || v > 255) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void map(DeviceRequestDto x, Device d) {
        d.setName(x.getName().trim());
        d.setIpAddress(x.getIpAddress().trim());
        d.setHostname(x.getHostname());
        d.setDeviceType(x.getDeviceType());
        d.setVendor(x.getVendor());
        d.setModel(x.getModel());
        d.setMacAddress(x.getMacAddress());
        d.setOsClue(x.getOsClue());
        if (x.getMonitoringEnabled() != null) d.setMonitoringEnabled(x.getMonitoringEnabled());
        if (x.getScanInterval() != null) d.setScanInterval(x.getScanInterval());
    }
}
