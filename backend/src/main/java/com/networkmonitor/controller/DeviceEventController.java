package com.networkmonitor.controller;

import com.networkmonitor.dto.DeviceEventDto;
import com.networkmonitor.repository.DeviceEventRepository;
import com.networkmonitor.repository.DeviceRepository;
import com.networkmonitor.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class DeviceEventController{
 private final DeviceEventRepository events; private final DeviceRepository devices;
 public DeviceEventController(DeviceEventRepository e,DeviceRepository d){events=e;devices=d;}
 @GetMapping public ResponseEntity<List<DeviceEventDto>> getRecent(){return ResponseEntity.ok(events.findTop100ByOrderByEventTimeDesc().stream().map(DeviceEventDto::from).collect(Collectors.toList()));}
 @GetMapping("/device/{deviceId}") public ResponseEntity<List<DeviceEventDto>> getDevice(@PathVariable Long deviceId){if(!devices.existsById(deviceId))throw new ResourceNotFoundException("Device not found with id: "+deviceId);return ResponseEntity.ok(events.findTop100ByDeviceIdOrderByEventTimeDesc(deviceId).stream().map(DeviceEventDto::from).collect(Collectors.toList()));}
}