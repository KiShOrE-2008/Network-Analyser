package com.networkmonitor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networkmonitor.dto.DeviceRequestDto;
import com.networkmonitor.dto.DeviceResponseDto;
import com.networkmonitor.entity.DeviceStatus;
import com.networkmonitor.entity.DeviceType;
import com.networkmonitor.entity.HealthStatus;
import com.networkmonitor.exception.ResourceNotFoundException;
import com.networkmonitor.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeviceService deviceService;

    private DeviceResponseDto responseDto;
    private DeviceRequestDto requestDto;

    @BeforeEach
    void setUp() {
        responseDto = new DeviceResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Core Router");
        responseDto.setIpAddress("192.168.1.1");
        responseDto.setDeviceType(DeviceType.ROUTER);
        responseDto.setStatus(DeviceStatus.ONLINE);
        responseDto.setHealthStatus(HealthStatus.HEALTHY);
        responseDto.setMonitoringEnabled(true);
        responseDto.setScanInterval(10);

        requestDto = new DeviceRequestDto();
        requestDto.setName("Core Router");
        requestDto.setIpAddress("192.168.1.1");
        requestDto.setDeviceType(DeviceType.ROUTER);
        requestDto.setScanInterval(10);
    }

    @Test
    @DisplayName("GET /api/devices should return 200 OK and list of devices")
    void getAllDevices_ShouldReturnDeviceList() throws Exception {
        when(deviceService.getAllDevices(null, null, null)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Core Router")))
                .andExpect(jsonPath("$[0].ipAddress", is("192.168.1.1")));
    }

    @Test
    @DisplayName("GET /api/devices/{id} should return 200 OK when device exists")
    void getDeviceById_ShouldReturnDevice() throws Exception {
        when(deviceService.getDeviceById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/devices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Core Router")));
    }

    @Test
    @DisplayName("GET /api/devices/{id} should return 404 NOT FOUND when device does not exist")
    void getDeviceById_ShouldReturn404WhenNotFound() throws Exception {
        when(deviceService.getDeviceById(99L)).thenThrow(new ResourceNotFoundException("Device not found with id: 99"));

        mockMvc.perform(get("/api/devices/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Device not found with id: 99")));
    }

    @Test
    @DisplayName("POST /api/devices should return 201 CREATED when valid request payload")
    void createDevice_ShouldReturn201() throws Exception {
        when(deviceService.createDevice(any(DeviceRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Core Router")));
    }

    @Test
    @DisplayName("POST /api/devices should return 400 BAD REQUEST when invalid IP address format")
    void createDevice_InvalidIp_ShouldReturn400() throws Exception {
        requestDto.setIpAddress("invalid_ip");
        when(deviceService.createDevice(any())).thenThrow(new IllegalArgumentException("Invalid IP address: invalid_ip"));

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("PATCH /api/devices/{id}/toggle-monitoring should return 200 OK")
    void toggleMonitoring_ShouldReturn200() throws Exception {
        responseDto.setMonitoringEnabled(false);
        when(deviceService.toggleMonitoring(1L)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/devices/1/toggle-monitoring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monitoringEnabled", is(false)));
    }

    @Test
    @DisplayName("DELETE /api/devices/{id} should return 204 NO CONTENT")
    void deleteDevice_ShouldReturn204() throws Exception {
        doNothing().when(deviceService).deleteDevice(1L);

        mockMvc.perform(delete("/api/devices/1"))
                .andExpect(status().isNoContent());
    }
}
