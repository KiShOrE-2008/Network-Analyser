package com.networkmonitor.repository;

import com.networkmonitor.entity.MonitoringMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitoringMetricRepository extends JpaRepository<MonitoringMetric, Long> {

    List<MonitoringMetric> findByDeviceIdOrderByTimestampDesc(Long deviceId);

    List<MonitoringMetric> findTop50ByDeviceIdOrderByTimestampDesc(Long deviceId);
}
