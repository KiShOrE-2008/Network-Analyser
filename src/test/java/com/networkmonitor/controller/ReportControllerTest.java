package com.networkmonitor.controller;

import com.networkmonitor.dto.SystemReportDto;
import com.networkmonitor.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    @DisplayName("GET /api/reports/summary should return 200 OK and summary report JSON")
    void getSummaryReport_ShouldReturn200() throws Exception {
        SystemReportDto report = new SystemReportDto();
        report.setTotalDevices(10);
        report.setOnlineDevices(9);
        report.setOfflineDevices(1);
        report.setSlaAvailabilityPercent(90.0);

        when(reportService.generateSystemSummaryReport()).thenReturn(report);

        mockMvc.perform(get("/api/reports/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDevices", is(10)))
                .andExpect(jsonPath("$.slaAvailabilityPercent", is(90.0)));
    }

    @Test
    @DisplayName("GET /api/reports/export/csv should return 200 OK with CSV attachment header")
    void exportDevicesCsv_ShouldReturnCsvFile() throws Exception {
        String csvContent = "ID,Name,IP Address\n1,Router,192.168.1.1\n";

        when(reportService.exportDevicesCsv()).thenReturn(csvContent);

        mockMvc.perform(get("/api/reports/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=device_inventory_report.csv")))
                .andExpect(content().string(containsString("1,Router,192.168.1.1")));
    }
}
