package com.networkmonitor.service;

import com.networkmonitor.dto.SpeedTestResultDto;
import com.networkmonitor.entity.NetworkSpeedTest;
import com.networkmonitor.repository.NetworkSpeedTestRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpeedTestService {

    private final NetworkSpeedTestRepository speedTestRepository;

    public SpeedTestService(NetworkSpeedTestRepository speedTestRepository) {
        this.speedTestRepository = speedTestRepository;
    }

    public byte[] generateDownloadBuffer(Integer sizeMb) {
        int targetMb = (sizeMb != null) ? Math.min(Math.max(sizeMb, 1), 100) : 25;
        int totalBytes = targetMb * 1024 * 1024;
        byte[] buffer = new byte[totalBytes];
        Arrays.fill(buffer, (byte) 0x5A); // Fill payload buffer
        return buffer;
    }

    public SpeedTestResultDto recordSpeedTest(SpeedTestResultDto requestDto) {
        NetworkSpeedTest entity = new NetworkSpeedTest();
        entity.setDeviceId(requestDto.getDeviceId());
        entity.setTestType(requestDto.getTestType() != null ? requestDto.getTestType() : "BROWSER_TO_SERVER");
        entity.setDownloadMbps(requestDto.getDownloadMbps());
        entity.setUploadMbps(requestDto.getUploadMbps());
        entity.setLatencyMs(requestDto.getLatencyMs());
        entity.setJitterMs(requestDto.getJitterMs());
        entity.setPacketLossPercent(requestDto.getPacketLossPercent());
        entity.setDurationMs(requestDto.getDurationMs());
        entity.setStatus(requestDto.getStatus() != null ? requestDto.getStatus() : "SUCCESS");

        speedTestRepository.save(entity);
        return SpeedTestResultDto.fromEntity(entity);
    }

    public List<SpeedTestResultDto> getSpeedTestHistory() {
        return speedTestRepository.findTop50ByOrderByTestedAtDesc()
                .stream()
                .map(SpeedTestResultDto::fromEntity)
                .collect(Collectors.toList());
    }
}
