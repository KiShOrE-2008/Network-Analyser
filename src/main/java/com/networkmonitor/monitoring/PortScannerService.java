package com.networkmonitor.monitoring;

import com.networkmonitor.entity.PortState;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PortScannerService {

    public static final Map<Integer, String> COMMON_PORTS = new LinkedHashMap<>();

    static {
        COMMON_PORTS.put(22, "SSH");
        COMMON_PORTS.put(53, "DNS");
        COMMON_PORTS.put(80, "HTTP");
        COMMON_PORTS.put(443, "HTTPS");
        COMMON_PORTS.put(445, "SMB");
        COMMON_PORTS.put(3306, "MySQL");
        COMMON_PORTS.put(5432, "PostgreSQL");
        COMMON_PORTS.put(3389, "RDP");
        COMMON_PORTS.put(8080, "HTTP-ALT");
    }

    public PortScanResult scanPort(String ipAddress, int port, String serviceName, int timeoutMs) {
        PortScanResult result = new PortScanResult();
        result.setIpAddress(ipAddress);
        result.setPort(port);
        result.setServiceName(serviceName != null ? serviceName : resolveServiceName(port));

        long startTime = System.nanoTime();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, port), timeoutMs);
            long endTime = System.nanoTime();
            double latency = (endTime - startTime) / 1_000_000.0;

            result.setState(PortState.OPEN);
            result.setLatencyMs(Math.round(latency * 100.0) / 100.0);
        } catch (SocketTimeoutException e) {
            result.setState(PortState.FILTERED);
            result.setLatencyMs(null);
        } catch (Exception e) {
            result.setState(PortState.CLOSED);
            result.setLatencyMs(null);
        }

        return result;
    }

    public String resolveServiceName(int port) {
        return COMMON_PORTS.getOrDefault(port, "UNKNOWN");
    }

    public static class PortScanResult {
        private String ipAddress;
        private int port;
        private String serviceName;
        private PortState state;
        private Double latencyMs;

        public PortScanResult() {
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public PortState getState() {
            return state;
        }

        public void setState(PortState state) {
            this.state = state;
        }

        public Double getLatencyMs() {
            return latencyMs;
        }

        public void setLatencyMs(Double latencyMs) {
            this.latencyMs = latencyMs;
        }
    }
}
