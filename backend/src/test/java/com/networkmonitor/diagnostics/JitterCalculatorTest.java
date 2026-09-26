package com.networkmonitor.diagnostics;

import com.networkmonitor.monitoring.JitterCalculator;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JitterCalculatorTest {

    @Test
    void testCalculateJitterMs() {
        List<Double> latencies = Arrays.asList(3.1, 3.3, 3.2, 7.1, 3.4);
        // Differences: 0.2, 0.1, 3.9, 3.7 -> sum = 7.9 -> avg = 7.9 / 4 = 1.975 -> rounded to 1.98
        double jitter = JitterCalculator.calculateJitterMs(latencies);
        assertEquals(1.98, jitter, 0.05);
    }

    @Test
    void testCalculateJitterSingleSample() {
        double jitter = JitterCalculator.calculateJitterMs(Collections.singletonList(5.0));
        assertEquals(0.0, jitter);
    }

    @Test
    void testCalculateJitterNullList() {
        double jitter = JitterCalculator.calculateJitterMs(null);
        assertEquals(0.0, jitter);
    }
}
