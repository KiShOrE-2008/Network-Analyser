package com.networkmonitor.repository;

import com.networkmonitor.entity.InterfaceMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterfaceMetricRepository extends JpaRepository<InterfaceMetric, Long> {
    List<InterfaceMetric> findTop50ByDeviceIdOrderBySampledAtDesc(Long deviceId);
    List<InterfaceMetric> findTop20ByDeviceIdAndInterfaceNameOrderBySampledAtDesc(Long deviceId, String interfaceName);
}
