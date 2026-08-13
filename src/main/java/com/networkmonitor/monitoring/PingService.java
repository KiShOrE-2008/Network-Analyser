package com.networkmonitor.monitoring;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PingService {

    private static final Pattern PACKET_LOSS_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)%\\s+packet\\s+loss");
    private static final Pattern RTT_AVG_PATTERN = Pattern.compile("rtt\\s+min/avg/max/mdev\\s*=\\s*\\d+(?:\\.\\d+)?/(\\d+(?:\\.\\d+)?)/");

    public PingResult ping(String ipAddress) {
        return ping(ipAddress, 3, 2);
    }

    public PingResult ping(String ipAddress, int packets, int timeoutSeconds) {
        PingResult result = new PingResult();
        result.setIpAddress(ipAddress);
        result.setTimestamp(LocalDateTime.now());

        try {
            ProcessBuilder pb = new ProcessBuilder("ping", "-c", String.valueOf(packets), "-W", String.valueOf(timeoutSeconds), ipAddress);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            String response = output.toString();

            Matcher lossMatcher = PACKET_LOSS_PATTERN.matcher(response);
            Matcher rttMatcher = RTT_AVG_PATTERN.matcher(response);

            Double packetLoss = null;
            if (lossMatcher.find()) {
                packetLoss = Double.parseDouble(lossMatcher.group(1));
            }

            Double avgLatency = null;
            if (rttMatcher.find()) {
                avgLatency = Double.parseDouble(rttMatcher.group(1));
            }

            boolean isReachable = (exitCode == 0) || (packetLoss != null && packetLoss < 100.0);

            if (isReachable) {
                result.setReachable(true);
                result.setPacketLossPercent(packetLoss != null ? packetLoss : 0.0);
                result.setLatencyMs(avgLatency != null ? avgLatency : 1.0);
            } else {
                // Fallback to InetAddress.isReachable if process return code failed or was unparsed
                return fallbackJavaPing(ipAddress, timeoutSeconds * 1000);
            }
        } catch (Exception ex) {
            return fallbackJavaPing(ipAddress, timeoutSeconds * 1000);
        }

        return result;
    }

    private PingResult fallbackJavaPing(String ipAddress, int timeoutMs) {
        PingResult result = new PingResult();
        result.setIpAddress(ipAddress);
        result.setTimestamp(LocalDateTime.now());

        try {
            long startTime = System.nanoTime();
            InetAddress address = InetAddress.getByName(ipAddress);
            boolean reachable = address.isReachable(timeoutMs);
            long endTime = System.nanoTime();

            double latency = (endTime - startTime) / 1_000_000.0;

            result.setReachable(reachable);
            if (reachable) {
                result.setLatencyMs(Math.round(latency * 100.0) / 100.0);
                result.setPacketLossPercent(0.0);
            } else {
                result.setLatencyMs(null);
                result.setPacketLossPercent(100.0);
            }
        } catch (Exception e) {
            result.setReachable(false);
            result.setLatencyMs(null);
            result.setPacketLossPercent(100.0);
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }
}
