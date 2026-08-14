package com.networkmonitor.repository;

import com.networkmonitor.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByIsResolvedFalseOrderByCreatedAtDesc();

    List<Alert> findByDeviceIdAndIsResolvedFalse(Long deviceId);

    List<Alert> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);

    List<Alert> findAllByOrderByCreatedAtDesc();
}
