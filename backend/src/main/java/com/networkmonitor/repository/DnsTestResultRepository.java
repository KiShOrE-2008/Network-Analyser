package com.networkmonitor.repository;

import com.networkmonitor.entity.DnsTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DnsTestResultRepository extends JpaRepository<DnsTestResult, Long> {
    List<DnsTestResult> findTop20ByOrderByTestedAtDesc();
}
