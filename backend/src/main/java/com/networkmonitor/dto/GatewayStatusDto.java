package com.networkmonitor.dto;

import com.networkmonitor.entity.GatewayMetric;
import java.time.LocalDateTime;

public class GatewayStatusDto {

    private String gatewayIp;
    private String localIp;
    private String networkCidr;
    private String interfaceName;
    private Double latencyMs;
    private Double packetLossPercent;
    private Double jitterMs;
    private boolean reachable;
    private LocalDateTime checkedAt;

    public GatewayStatusDto() {
    }

    public static GatewayStatusDto fromEntity(GatewayMetric entity) {
        GatewayStatusDto dto = new GatewayStatusDto();
        dto.setGatewayIp(entity.getGatewayIp());
        dto.setLocalIp(entity.getLocalIp());
        dto.setNetworkCidr(entity.getNetworkCidr());
        dto.setInterfaceName(entity.getInterfaceName());
        dto.setLatencyMs(entity.getLatencyMs());
        dto.setPacketLossPercent(entity.getPacketLossPercent());
        dto.setJitterMs(entity.getJitterMs());
        dto.setReachable(entity.isReachable());
        dto.setCheckedAt(entity.getCheckedAt());
        return dto;
    }

    public String getGatewayIp() { return gatewayIp; }
    public void setGatewayIp(String gatewayIp) { this.gatewayIp = gatewayIp; }

    public String getLocalIp() { return localIp; }
    public void setLocalIp(String localIp) { this.localIp = localIp; }

    public String getNetworkCidr() { return networkCidr; }
    public void setNetworkCidr(String networkCidr) { this.networkCidr = networkCidr; }

    public String getInterfaceName() { return interfaceName; }
    public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

    public Double getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Double latencyMs) { this.latencyMs = latencyMs; }

    public Double getPacketLossPercent() { return packetLossPercent; }
    public void setPacketLossPercent(Double packetLossPercent) { this.packetLossPercent = packetLossPercent; }

    public Double getJitterMs() { return jitterMs; }
    public void setJitterMs(Double jitterMs) { this.jitterMs = jitterMs; }

    public boolean isReachable() { return reachable; }
    public void setReachable(boolean reachable) { this.reachable = reachable; }

    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
}
