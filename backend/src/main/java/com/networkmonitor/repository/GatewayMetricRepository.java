package com.networkmonitor.repository;

import com.networkmonitor.entity.GatewayMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GatewayMetricRepository extends JpaRepository<GatewayMetric, Long> {
    List<GatewayMetric> findTop20ByOrderByCheckedAtDesc();
}
