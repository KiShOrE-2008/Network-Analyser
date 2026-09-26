package com.networkmonitor.dto;

import com.networkmonitor.entity.DnsTestResult;
import java.time.LocalDateTime;

public class DnsTestResultDto {

    private Long id;
    private String resolver;
    private String hostname;
    private Double responseTimeMs;
    private String resolvedIp;
    private boolean success;
    private LocalDateTime testedAt;

    public DnsTestResultDto() {
    }

    public static DnsTestResultDto fromEntity(DnsTestResult entity) {
        DnsTestResultDto dto = new DnsTestResultDto();
        dto.setId(entity.getId());
        dto.setResolver(entity.getResolver());
        dto.setHostname(entity.getHostname());
        dto.setResponseTimeMs(entity.getResponseTimeMs());
        dto.setResolvedIp(entity.getResolvedIp());
        dto.setSuccess(entity.isSuccess());
        dto.setTestedAt(entity.getTestedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getResolver() { return resolver; }
    public void setResolver(String resolver) { this.resolver = resolver; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public Double getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Double responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public String getResolvedIp() { return resolvedIp; }
    public void setResolvedIp(String resolvedIp) { this.resolvedIp = resolvedIp; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public LocalDateTime getTestedAt() { return testedAt; }
    public void setTestedAt(LocalDateTime testedAt) { this.testedAt = testedAt; }
}
