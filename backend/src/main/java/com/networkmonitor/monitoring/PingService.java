package com.networkmonitor.monitoring;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PingService {

    private static final Pattern LINUX_PACKET_LOSS_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)%\\s+packet\\s+loss");
    private static final Pattern LINUX_RTT_AVG_PATTERN = Pattern.compile("rtt\\s+min/avg/max/mdev\\s*=\\s*\\d+(?:\\.\\d+)?/(\\d+(?:\\.\\d+)?)/");

    private static final Pattern WIN_PACKET_LOSS_PATTERN = Pattern.compile("\\((\\d+(?:\\.\\d+)?)%\\s+loss\\)");
    private static final Pattern WIN_RTT_AVG_PATTERN = Pattern.compile("Average\\s*=\\s*(\\d+)ms");

    public PingResult ping(String ipAddress) {
        return ping(ipAddress, 3, 2);
    }

    public PingResult ping(String ipAddress, int packets, int timeoutSeconds) {
        PingResult result = new PingResult();
        result.setIpAddress(ipAddress);
        result.setTimestamp(LocalDateTime.now());

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        try {
            ProcessBuilder pb;
            if (isWindows) {
                // Windows ping uses -n for count, -w for timeout in milliseconds
                pb = new ProcessBuilder("ping", "-n", String.valueOf(packets), "-w", String.valueOf(timeoutSeconds * 1000), ipAddress);
            } else {
                // Linux/macOS ping uses -c for count, -W for timeout in seconds
                pb = new ProcessBuilder("ping", "-c", String.valueOf(packets), "-W", String.valueOf(timeoutSeconds), ipAddress);
            }

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Java-side process timeout guard to prevent hangs
            boolean completed = process.waitFor(timeoutSeconds + 3, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                return fallbackJavaPing(ipAddress, timeoutSeconds * 1000);
            }

            int exitCode = process.exitValue();
            String response = output.toString();

            Double packetLoss = null;
            Double avgLatency = null;

            if (isWindows) {
                Matcher lossMatcher = WIN_PACKET_LOSS_PATTERN.matcher(response);
                if (lossMatcher.find()) {
                    packetLoss = Double.parseDouble(lossMatcher.group(1));
                }
                Matcher rttMatcher = WIN_RTT_AVG_PATTERN.matcher(response);
                if (rttMatcher.find()) {
                    avgLatency = Double.parseDouble(rttMatcher.group(1));
                }
            } else {
                Matcher lossMatcher = LINUX_PACKET_LOSS_PATTERN.matcher(response);
                if (lossMatcher.find()) {
                    packetLoss = Double.parseDouble(lossMatcher.group(1));
                }
                Matcher rttMatcher = LINUX_RTT_AVG_PATTERN.matcher(response);
                if (rttMatcher.find()) {
                    avgLatency = Double.parseDouble(rttMatcher.group(1));
                }
            }

            boolean isReachable = (exitCode == 0) || (packetLoss != null && packetLoss < 100.0);

            if (isReachable) {
                result.setReachable(true);
                result.setPacketLossPercent(packetLoss != null ? packetLoss : 0.0);
                result.setLatencyMs(avgLatency != null ? avgLatency : 1.0);
            } else {
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
