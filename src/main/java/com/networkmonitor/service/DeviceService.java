package com.networkmonitor.service;

import com.networkmonitor.dto.DeviceRequestDto;
import com.networkmonitor.dto.DeviceResponseDto;
import com.networkmonitor.entity.Device;
import com.networkmonitor.entity.DeviceStatus;
import com.networkmonitor.entity.DeviceType;
import com.networkmonitor.exception.ResourceNotFoundException;
import com.networkmonitor.repository.DeviceRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public List<DeviceResponseDto> getAllDevices(String search, DeviceType type, DeviceStatus status) {
        Specification<Device> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(cb.equal(root.get("deviceType"), type));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate ipMatch = cb.like(root.get("ipAddress"), pattern);
                Predicate hostMatch = cb.like(cb.lower(root.get("hostname")), pattern);
                predicates.add(cb.or(nameMatch, ipMatch, hostMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return deviceRepository.findAll(spec)
                .stream()
                .map(DeviceResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeviceResponseDto getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
        return DeviceResponseDto.fromEntity(device);
    }

    @Transactional
    public DeviceResponseDto createDevice(DeviceRequestDto dto) {
        validateDeviceRequest(dto);

        if (deviceRepository.existsByIpAddress(dto.getIpAddress())) {
            throw new IllegalArgumentException("Device with IP address '" + dto.getIpAddress() + "' already exists.");
        }

        Device device = new Device();
        mapDtoToEntity(dto, device);

        Device savedDevice = deviceRepository.save(device);
        return DeviceResponseDto.fromEntity(savedDevice);
    }

    @Transactional
    public DeviceResponseDto updateDevice(Long id, DeviceRequestDto dto) {
        validateDeviceRequest(dto);

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        if (!device.getIpAddress().equalsIgnoreCase(dto.getIpAddress()) && deviceRepository.existsByIpAddress(dto.getIpAddress())) {
            throw new IllegalArgumentException("Device with IP address '" + dto.getIpAddress() + "' already exists.");
        }

        mapDtoToEntity(dto, device);
        Device updatedDevice = deviceRepository.save(device);
        return DeviceResponseDto.fromEntity(updatedDevice);
    }

    @Transactional
    public DeviceResponseDto toggleMonitoring(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
        device.setMonitoringEnabled(!device.isMonitoringEnabled());
        Device updated = deviceRepository.save(device);
        return DeviceResponseDto.fromEntity(updated);
    }

    @Transactional
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Device not found with id: " + id);
        }
        deviceRepository.deleteById(id);
    }

    private void validateDeviceRequest(DeviceRequestDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Device name cannot be empty.");
        }
        if (dto.getIpAddress() == null || dto.getIpAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("IP Address cannot be empty.");
        }
        if (!isValidIpAddress(dto.getIpAddress())) {
            throw new IllegalArgumentException("Invalid IP address: " + dto.getIpAddress());
        }
        if (dto.getScanInterval() != null && dto.getScanInterval() <= 0) {
            throw new IllegalArgumentException("Scan interval must be a positive integer greater than 0.");
        }
    }

    private boolean isValidIpAddress(String ip) {
        try {
            if (ip == null || ip.isEmpty()) return false;
            String[] parts = ip.split("\\.");
            if (parts.length != 4) return false;
            for (String p : parts) {
                int val = Integer.parseInt(p);
                if (val < 0 || val > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void mapDtoToEntity(DeviceRequestDto dto, Device device) {
        device.setName(dto.getName().trim());
        device.setIpAddress(dto.getIpAddress().trim());
        device.setHostname(dto.getHostname() != null ? dto.getHostname().trim() : null);
        device.setDeviceType(dto.getDeviceType());
        device.setVendor(dto.getVendor() != null ? dto.getVendor().trim() : null);
        device.setModel(dto.getModel() != null ? dto.getModel().trim() : null);
        if (dto.getMonitoringEnabled() != null) {
            device.setMonitoringEnabled(dto.getMonitoringEnabled());
        }
        if (dto.getScanInterval() != null) {
            device.setScanInterval(dto.getScanInterval());
        }
    }
}

