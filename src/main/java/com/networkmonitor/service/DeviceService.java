package com.networkmonitor.service;

import com.networkmonitor.dto.DeviceRequestDto;
import com.networkmonitor.dto.DeviceResponseDto;
import com.networkmonitor.entity.Device;
import com.networkmonitor.exception.ResourceNotFoundException;
import com.networkmonitor.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public List<DeviceResponseDto> getAllDevices() {
        return deviceRepository.findAll()
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
        if (deviceRepository.existsByIpAddress(dto.getIpAddress())) {
            throw new IllegalArgumentException("Device with IP address " + dto.getIpAddress() + " already exists.");
        }

        Device device = new Device();
        mapDtoToEntity(dto, device);

        Device savedDevice = deviceRepository.save(device);
        return DeviceResponseDto.fromEntity(savedDevice);
    }

    @Transactional
    public DeviceResponseDto updateDevice(Long id, DeviceRequestDto dto) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        if (!device.getIpAddress().equals(dto.getIpAddress()) && deviceRepository.existsByIpAddress(dto.getIpAddress())) {
            throw new IllegalArgumentException("Device with IP address " + dto.getIpAddress() + " already exists.");
        }

        mapDtoToEntity(dto, device);
        Device updatedDevice = deviceRepository.save(device);
        return DeviceResponseDto.fromEntity(updatedDevice);
    }

    @Transactional
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Device not found with id: " + id);
        }
        deviceRepository.deleteById(id);
    }

    private void mapDtoToEntity(DeviceRequestDto dto, Device device) {
        device.setName(dto.getName());
        device.setIpAddress(dto.getIpAddress());
        device.setHostname(dto.getHostname());
        device.setDeviceType(dto.getDeviceType());
        device.setVendor(dto.getVendor());
        device.setModel(dto.getModel());
        if (dto.getMonitoringEnabled() != null) {
            device.setMonitoringEnabled(dto.getMonitoringEnabled());
        }
        if (dto.getScanInterval() != null) {
            device.setScanInterval(dto.getScanInterval());
        }
    }
}
