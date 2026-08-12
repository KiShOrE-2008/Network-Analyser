package com.networkmonitor.repository;

import com.networkmonitor.entity.Device;
import com.networkmonitor.entity.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByIpAddress(String ipAddress);

    boolean existsByIpAddress(String ipAddress);

    List<Device> findByStatus(DeviceStatus status);

    List<Device> findByMonitoringEnabledTrue();
}
