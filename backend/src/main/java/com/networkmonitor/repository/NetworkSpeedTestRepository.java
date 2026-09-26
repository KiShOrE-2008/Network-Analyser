package com.networkmonitor.repository;

import com.networkmonitor.entity.NetworkSpeedTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkSpeedTestRepository extends JpaRepository<NetworkSpeedTest, Long> {
    List<NetworkSpeedTest> findTop50ByOrderByTestedAtDesc();
    List<NetworkSpeedTest> findTop20ByDeviceIdOrderByTestedAtDesc(Long deviceId);
}
