package com.networkmonitor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gateway_metrics")
public class GatewayMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gatewayIp;
    private String localIp;
    private String networkCidr;
    private String interfaceName;
    private Double latencyMs;
    private Double packetLossPercent;
    private Double jitterMs;
    private boolean reachable;
    private LocalDateTime checkedAt;

    public GatewayMetric() {
        this.checkedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
