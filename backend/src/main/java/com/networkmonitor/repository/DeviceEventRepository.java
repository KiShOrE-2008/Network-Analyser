package com.networkmonitor.repository;

import com.networkmonitor.entity.DeviceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DeviceEventRepository extends JpaRepository<DeviceEvent, Long> {
    @Query(value = "SELECT * FROM device_events WHERE device_id = :deviceId ORDER BY event_time DESC LIMIT 100", nativeQuery = true)
    List<DeviceEvent> findTop100ByDeviceIdOrderByEventTimeDesc(@Param("deviceId") Long deviceId);

    List<DeviceEvent> findTop100ByOrderByEventTimeDesc();
}