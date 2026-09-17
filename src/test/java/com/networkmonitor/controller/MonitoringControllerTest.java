package com.networkmonitor.controller;

import com.networkmonitor.dto.MetricResponseDto;
import com.networkmonitor.dto.PingCheckResponseDto;
import com.networkmonitor.entity.DeviceStatus;
import com.networkmonitor.exception.ResourceNotFoundException;
import com.networkmonitor.monitoring.NmapService;
import com.networkmonitor.monitoring.SnmpService;
import com.networkmonitor.repository.DeviceRepository;
import com.networkmonitor.service.DeviceMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MonitoringController.class)
class MonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceMonitoringService monitoringService;

    @MockitoBean
    private DeviceRepository deviceRepository;

    @MockitoBean
    private NmapService nmapService;

    @MockitoBean
    private SnmpService snmpService;

    private PingCheckResponseDto pingResponseDto;

    @BeforeEach
    void setUp() {
        pingResponseDto = new PingCheckResponseDto();
        pingResponseDto.setDeviceId(1L);
        pingResponseDto.setDeviceName("Core Router");
        pingResponseDto.setIpAddress("192.168.1.1");
        pingResponseDto.setReachable(true);
        pingResponseDto.setLatencyMs(2.4);
        pingResponseDto.setPacketLossPercent(0.0);
        pingResponseDto.setDeviceStatus(DeviceStatus.ONLINE);
        pingResponseDto.setCheckedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/devices/{id}/check should return 200 OK and ping result")
    void performPingCheck_ShouldReturn200() throws Exception {
        when(monitoringService.performPingCheck(1L)).thenReturn(pingResponseDto);

        mockMvc.perform(post("/api/devices/1/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId", is(1)))
                .andExpect(jsonPath("$.ipAddress", is("192.168.1.1")))
                .andExpect(jsonPath("$.reachable", is(true)))
                .andExpect(jsonPath("$.latencyMs", is(2.4)))
                .andExpect(jsonPath("$.deviceStatus", is("ONLINE")));
    }

    @Test
    @DisplayName("POST /api/devices/{id}/check should return 404 NOT FOUND when device missing")
    void performPingCheck_ShouldReturn404WhenNotFound() throws Exception {
        when(monitoringService.performPingCheck(99L)).thenThrow(new ResourceNotFoundException("Device not found with id: 99"));

        mockMvc.perform(post("/api/devices/99/check"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Device not found with id: 99")));
    }

    @Test
    @DisplayName("GET /api/devices/{id}/metrics should return 200 OK and metric history list")
    void getDeviceMetrics_ShouldReturnMetricsList() throws Exception {
        MetricResponseDto metricDto = new MetricResponseDto();
        metricDto.setId(10L);
        metricDto.setDeviceId(1L);
        metricDto.setReachable(true);
        metricDto.setLatencyMs(1.5);
        metricDto.setPacketLossPercent(0.0);
        metricDto.setTimestamp(LocalDateTime.now());

        when(monitoringService.getDeviceMetrics(1L)).thenReturn(List.of(metricDto));

        mockMvc.perform(get("/api/devices/1/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].deviceId", is(1)))
                .andExpect(jsonPath("$[0].latencyMs", is(1.5)));
    }
}
