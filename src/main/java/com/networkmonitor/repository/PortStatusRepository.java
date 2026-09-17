package com.networkmonitor.repository;

import com.networkmonitor.entity.PortStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortStatusRepository extends JpaRepository<PortStatus, Long> {

    List<PortStatus> findByDeviceId(Long deviceId);

    void deleteByDeviceId(Long deviceId);
}
