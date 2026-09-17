package com.networkmonitor.repository;

import com.networkmonitor.entity.DeviceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeviceEventRepository extends JpaRepository<DeviceEvent,Long>{
 List<DeviceEvent> findTop100ByDeviceIdOrderByEventTimeDesc(Long deviceId);
 List<DeviceEvent> findTop100ByOrderByEventTimeDesc();
}