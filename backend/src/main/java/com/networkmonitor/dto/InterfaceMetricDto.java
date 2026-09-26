package com.networkmonitor.dto;

import com.networkmonitor.entity.InterfaceMetric;
import java.time.LocalDateTime;

public class InterfaceMetricDto {

    private Long id;
    private Long deviceId;
    private String interfaceName;
    private Long rxBytes;
    private Long txBytes;
    private Long rxPackets;
    private Long txPackets;
    private Long rxErrors;
    private Long txErrors;
    private Long rxDrops;
    private Long txDrops;
    private Double rxMbps;
    private Double txMbps;
    private LocalDateTime sampledAt;

    public InterfaceMetricDto() {
    }

    public static InterfaceMetricDto fromEntity(InterfaceMetric entity) {
        InterfaceMetricDto dto = new InterfaceMetricDto();
        dto.setId(entity.getId());
        dto.setDeviceId(entity.getDeviceId());
        dto.setInterfaceName(entity.getInterfaceName());
        dto.setRxBytes(entity.getRxBytes());
        dto.setTxBytes(entity.getTxBytes());
        dto.setRxPackets(entity.getRxPackets());
        dto.setTxPackets(entity.getTxPackets());
        dto.setRxErrors(entity.getRxErrors());
        dto.setTxErrors(entity.getTxErrors());
        dto.setRxDrops(entity.getRxDrops());
        dto.setTxDrops(entity.getTxDrops());
        dto.setRxMbps(entity.getRxMbps());
        dto.setTxMbps(entity.getTxMbps());
        dto.setSampledAt(entity.getSampledAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public String getInterfaceName() { return interfaceName; }
    public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

    public Long getRxBytes() { return rxBytes; }
    public void setRxBytes(Long rxBytes) { this.rxBytes = rxBytes; }

    public Long getTxBytes() { return txBytes; }
    public void setTxBytes(Long txBytes) { this.txBytes = txBytes; }

    public Long getRxPackets() { return rxPackets; }
    public void setRxPackets(Long rxPackets) { this.rxPackets = rxPackets; }

    public Long getTxPackets() { return txPackets; }
    public void setTxPackets(Long txPackets) { this.txPackets = txPackets; }

    public Long getRxErrors() { return rxErrors; }
    public void setRxErrors(Long rxErrors) { this.rxErrors = rxErrors; }

    public Long getTxErrors() { return txErrors; }
    public void setTxErrors(Long txErrors) { this.txErrors = txErrors; }

    public Long getRxDrops() { return rxDrops; }
    public void setRxDrops(Long rxDrops) { this.rxDrops = rxDrops; }

    public Long getTxDrops() { return txDrops; }
    public void setTxDrops(Long txDrops) { this.txDrops = txDrops; }

    public Double getRxMbps() { return rxMbps; }
    public void setRxMbps(Double rxMbps) { this.rxMbps = rxMbps; }

    public Double getTxMbps() { return txMbps; }
    public void setTxMbps(Double txMbps) { this.txMbps = txMbps; }

    public LocalDateTime getSampledAt() { return sampledAt; }
    public void setSampledAt(LocalDateTime sampledAt) { this.sampledAt = sampledAt; }
}
