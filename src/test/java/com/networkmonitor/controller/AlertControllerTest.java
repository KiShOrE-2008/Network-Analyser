package com.networkmonitor.controller;

import com.networkmonitor.dto.AlertResponseDto;
import com.networkmonitor.entity.AlertSeverity;
import com.networkmonitor.entity.AlertType;
import com.networkmonitor.service.AlertService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertService alertService;

    private AlertResponseDto alertDto;

    @BeforeEach
    void setUp() {
        alertDto = new AlertResponseDto();
        alertDto.setId(1L);
        alertDto.setDeviceId(10L);
        alertDto.setDeviceName("Gateway");
        alertDto.setDeviceIp("192.168.1.1");
        alertDto.setAlertType(AlertType.DEVICE_OFFLINE);
        alertDto.setSeverity(AlertSeverity.CRITICAL);
        alertDto.setMessage("Device Gateway (192.168.1.1) is UNREACHABLE.");
        alertDto.setResolved(false);
        alertDto.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/alerts should return 200 OK and active alert list")
    void getAlerts_ShouldReturnActiveAlerts() throws Exception {
        when(alertService.getActiveAlerts()).thenReturn(List.of(alertDto));

        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].severity", is("CRITICAL")));
    }

    @Test
    @DisplayName("PATCH /api/alerts/{id}/resolve should return 200 OK and updated alert")
    void resolveAlert_ShouldReturnResolvedAlert() throws Exception {
        alertDto.setResolved(true);
        alertDto.setResolvedAt(LocalDateTime.now());

        when(alertService.resolveAlert(1L)).thenReturn(alertDto);

        mockMvc.perform(patch("/api/alerts/1/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.resolved", is(true)));
    }
}
