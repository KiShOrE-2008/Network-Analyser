package com.networkmonitor.controller;

import com.networkmonitor.dto.SpeedTestResultDto;
import com.networkmonitor.service.SpeedTestService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnostics/speed-test")
public class SpeedTestController {

    private final SpeedTestService speedTestService;

    public SpeedTestController(SpeedTestService speedTestService) {
        this.speedTestService = speedTestService;
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadPayload(
            @RequestParam(required = false, defaultValue = "25") Integer sizeMb) {
        byte[] payload = speedTestService.generateDownloadBuffer(sizeMb);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=speedtest_payload.bin")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(payload.length)
                .body(payload);
    }

    @PostMapping("/upload")
    public ResponseEntity<SpeedTestResultDto> uploadPayload(
            @RequestBody(required = false) byte[] payload) {
        long sizeBytes = (payload != null) ? payload.length : 0;
        SpeedTestResultDto result = new SpeedTestResultDto();
        result.setTestType("BROWSER_TO_SERVER");
        result.setStatus("SUCCESS");
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<SpeedTestResultDto> recordSpeedTest(@RequestBody SpeedTestResultDto request) {
        return ResponseEntity.ok(speedTestService.recordSpeedTest(request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<SpeedTestResultDto>> getSpeedTestHistory() {
        return ResponseEntity.ok(speedTestService.getSpeedTestHistory());
    }
}
