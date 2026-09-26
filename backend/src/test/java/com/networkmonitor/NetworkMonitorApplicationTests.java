package com.networkmonitor;

import com.networkmonitor.repository.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NetworkMonitorApplicationTests {

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    void contextLoads() {
        assertThat(deviceRepository).isNotNull();
    }
}
