package com.networkmonitor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dns_test_results")
public class DnsTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resolver;
    private String hostname;
    private Double responseTimeMs;
    private String resolvedIp;
    private boolean success;
    private LocalDateTime testedAt;

    public DnsTestResult() {
        this.testedAt = LocalDateTime.now();
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
