package com.networkmonitor.repository;

import com.networkmonitor.entity.DeviceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DeviceEventRepository extends JpaRepository<DeviceEvent, Long> {
    @Query("SELECT e FROM DeviceEvent e WHERE e.device.id = :deviceId ORDER BY e.eventTime DESC")
    List<DeviceEvent> findTop100ByDeviceIdOrderByEventTimeDesc(@Param("deviceId") Long deviceId);

    List<DeviceEvent> findTop100ByOrderByEventTimeDesc();
}