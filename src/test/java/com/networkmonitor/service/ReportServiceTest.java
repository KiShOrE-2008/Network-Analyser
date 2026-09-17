package com.networkmonitor.service;

import com.networkmonitor.dto.SystemReportDto;
import com.networkmonitor.entity.Device;
import com.networkmonitor.entity.DeviceStatus;
import com.networkmonitor.entity.DeviceType;
import com.networkmonitor.repository.AlertRepository;
import com.networkmonitor.repository.DeviceRepository;
import com.networkmonitor.repository.MonitoringMetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private MonitoringMetricRepository metricRepository;

    @Mock
    private AlertRepository alertRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(deviceRepository, metricRepository, alertRepository);
    }

    @Test
    @DisplayName("generateSystemSummaryReport should compute correct SLA percentage and device counts")
    void generateSystemSummaryReport_Success() {
        Device d1 = new Device("Router", "192.168.1.1", DeviceType.ROUTER);
        d1.setStatus(DeviceStatus.ONLINE);
        Device d2 = new Device("Server", "192.168.1.2", DeviceType.SERVER);
        d2.setStatus(DeviceStatus.OFFLINE);

        when(deviceRepository.findAll()).thenReturn(List.of(d1, d2));
        when(metricRepository.count()).thenReturn(100L);
        when(alertRepository.count()).thenReturn(5L);
        when(alertRepository.findByIsResolvedFalseOrderByCreatedAtDesc()).thenReturn(new ArrayList<>());
        when(metricRepository.findAll()).thenReturn(new ArrayList<>());

        SystemReportDto report = reportService.generateSystemSummaryReport();

        assertThat(report.getTotalDevices()).isEqualTo(2);
        assertThat(report.getOnlineDevices()).isEqualTo(1);
        assertThat(report.getOfflineDevices()).isEqualTo(1);
        assertThat(report.getSlaAvailabilityPercent()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("exportDevicesCsv should format CSV header and rows correctly")
    void exportDevicesCsv_Success() {
        Device d1 = new Device("Core Router", "192.168.1.1", DeviceType.ROUTER);
        d1.setId(1L);

        when(deviceRepository.findAll()).thenReturn(List.of(d1));

        String csv = reportService.exportDevicesCsv();

        assertThat(csv).contains("ID,Name,IP Address");
        assertThat(csv).contains("1,Core Router,192.168.1.1");
    }
}
