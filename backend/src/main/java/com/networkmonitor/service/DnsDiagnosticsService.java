package com.networkmonitor.service;

import com.networkmonitor.dto.DnsTestRequestDto;
import com.networkmonitor.dto.DnsTestResultDto;
import com.networkmonitor.entity.DnsTestResult;
import com.networkmonitor.repository.DnsTestResultRepository;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
public class DnsDiagnosticsService {

    private final DnsTestResultRepository dnsTestResultRepository;

    public DnsDiagnosticsService(DnsTestResultRepository dnsTestResultRepository) {
        this.dnsTestResultRepository = dnsTestResultRepository;
    }

    public DnsTestResultDto testDnsResolution(DnsTestRequestDto request) {
        String hostname = (request != null && request.getHostname() != null && !request.getHostname().isBlank())
                ? request.getHostname().trim()
                : "example.com";

        long start = System.nanoTime();
        boolean success = false;
        String resolvedIp = null;
        String resolver = "System Default Resolver";

        try {
            InetAddress address = InetAddress.getByName(hostname);
            long elapsed = System.nanoTime() - start;
            resolvedIp = address.getHostAddress();
            success = true;
            double responseMs = Math.round((elapsed / 1_000_000.0) * 100.0) / 100.0;

            DnsTestResult entity = new DnsTestResult();
            entity.setHostname(hostname);
            entity.setResolver(resolver);
            entity.setResponseTimeMs(responseMs);
            entity.setResolvedIp(resolvedIp);
            entity.setSuccess(true);

            dnsTestResultRepository.save(entity);
            return DnsTestResultDto.fromEntity(entity);
        } catch (Exception e) {
            long elapsed = System.nanoTime() - start;
            double responseMs = Math.round((elapsed / 1_000_000.0) * 100.0) / 100.0;

            DnsTestResult entity = new DnsTestResult();
            entity.setHostname(hostname);
            entity.setResolver(resolver);
            entity.setResponseTimeMs(responseMs);
            entity.setResolvedIp(null);
            entity.setSuccess(false);

            dnsTestResultRepository.save(entity);
            return DnsTestResultDto.fromEntity(entity);
        }
    }
}
