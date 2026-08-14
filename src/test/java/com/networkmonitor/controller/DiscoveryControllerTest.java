package com.networkmonitor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networkmonitor.dto.DeviceRequestDto;
import com.networkmonitor.dto.DeviceResponseDto;
import com.networkmonitor.dto.DiscoveryRequestDto;
import com.networkmonitor.dto.DiscoveryResponseDto;
import com.networkmonitor.dto.NmapScanResultDto;
import com.networkmonitor.entity.DeviceType;
import com.networkmonitor.monitoring.NmapService;
import com.networkmonitor.service.DiscoveryService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiscoveryController.class)
class DiscoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DiscoveryService discoveryService;

    @MockitoBean
    private NmapService nmapService;

    private DiscoveryRequestDto requestDto;
    private DiscoveryResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new DiscoveryRequestDto();
        requestDto.setSubnetCidr("192.168.1.0/24");
        requestDto.setStrategy("PING");

        responseDto = new DiscoveryResponseDto();
        responseDto.setSubnetCidr("192.168.1.0/24");
        responseDto.setTotalScanned(254);
        responseDto.setDevicesDiscoveredCount(5);
        responseDto.setNewDevicesCount(3);
        responseDto.setExistingDevicesCount(2);
    }

    @Test
    @DisplayName("POST /api/discovery/scan should return 200 OK and discovery results")
    void scanSubnet_ShouldReturn200() throws Exception {
        when(discoveryService.scanSubnet(any(DiscoveryRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/discovery/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subnetCidr", is("192.168.1.0/24")))
                .andExpect(jsonPath("$.totalScanned", is(254)))
                .andExpect(jsonPath("$.devicesDiscoveredCount", is(5)));
    }

    @Test
    @DisplayName("POST /api/discovery/scan should return 400 BAD REQUEST for invalid CIDR notation")
    void scanSubnet_InvalidCidr_ShouldReturn400() throws Exception {
        requestDto.setSubnetCidr("invalid_cidr");

        mockMvc.perform(post("/api/discovery/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("POST /api/discovery/import should return 201 CREATED and imported device list")
    void importDevices_ShouldReturn201() throws Exception {
        DeviceRequestDto deviceDto = new DeviceRequestDto();
        deviceDto.setName("Imported Server");
        deviceDto.setIpAddress("192.168.1.100");
        deviceDto.setDeviceType(DeviceType.SERVER);

        DeviceResponseDto responseDevice = new DeviceResponseDto();
        responseDevice.setId(15L);
        responseDevice.setName("Imported Server");
        responseDevice.setIpAddress("192.168.1.100");

        when(discoveryService.importDiscoveredDevices(any())).thenReturn(List.of(responseDevice));

        mockMvc.perform(post("/api/discovery/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(deviceDto))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(15)))
                .andExpect(jsonPath("$[0].name", is("Imported Server")));
    }

    @Test
    @DisplayName("GET /api/discovery/nmap/status should return 200 OK and Nmap availability details")
    void getNmapStatus_ShouldReturn200() throws Exception {
        when(nmapService.isNmapAvailable()).thenReturn(true);

        mockMvc.perform(get("/api/discovery/nmap/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.binary", is("/usr/bin/nmap")));
    }
}
