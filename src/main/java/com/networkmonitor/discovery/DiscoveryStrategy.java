package com.networkmonitor.discovery;

public interface DiscoveryStrategy {

    boolean checkReachability(String ipAddress, int timeoutMs);

    String getStrategyName();
}
