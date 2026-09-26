package com.networkmonitor.monitoring;

import java.util.List;

public class JitterCalculator {

    /**
     * Calculates Mean Absolute Consecutive Difference Jitter:
     * Jitter = (|L2-L1| + |L3-L2| + ... + |Ln-Ln-1|) / (n - 1)
     */
    public static double calculateJitterMs(List<Double> latencySamples) {
        if (latencySamples == null || latencySamples.size() < 2) {
            return 0.0;
        }

        double totalDiff = 0.0;
        int count = 0;

        for (int i = 1; i < latencySamples.size(); i++) {
            Double l1 = latencySamples.get(i - 1);
            Double l2 = latencySamples.get(i);
            if (l1 != null && l2 != null) {
                totalDiff += Math.abs(l2 - l1);
                count++;
            }
        }

        if (count == 0) return 0.0;
        return Math.round((totalDiff / count) * 100.0) / 100.0;
    }
}
