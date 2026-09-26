package com.networkmonitor.controller;

import com.networkmonitor.service.MonitoringSchedulerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchedulerController.class)
class SchedulerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MonitoringSchedulerService schedulerService;

    @Test
    @DisplayName("GET /api/scheduler/status should return 200 OK and scheduler status details")
    void getSchedulerStatus_ShouldReturnStatus() throws Exception {
        when(schedulerService.isSchedulerActive()).thenReturn(true);
        when(schedulerService.getWorkerPoolSize()).thenReturn(10);
        when(schedulerService.getLastDevicesScannedCount()).thenReturn(5);

        mockMvc.perform(get("/api/scheduler/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.workerPoolSize", is(10)));
    }

    @Test
    @DisplayName("POST /api/scheduler/stop should return 200 OK and updated status")
    void stopScheduler_ShouldReturnStatus() throws Exception {
        when(schedulerService.stopScheduler()).thenReturn(true);
        when(schedulerService.isSchedulerActive()).thenReturn(false);

        mockMvc.perform(post("/api/scheduler/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));
    }
}
