package com.networkmonitor.discovery;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class SubnetCalculator {

    public static List<String> getIpAddressesInCidr(String cidr) {
        if (cidr == null || !cidr.contains("/")) {
            throw new IllegalArgumentException("Invalid CIDR format. Example: 192.168.1.0/24");
        }

        String[] parts = cidr.split("/");
        String ipStr = parts[0].trim();
        int prefixLength;

        try {
            prefixLength = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid CIDR prefix: " + parts[1]);
        }

        if (prefixLength < 16 || prefixLength > 32) {
            throw new IllegalArgumentException("CIDR prefix must be between /16 and /32 for security and resource limits.");
        }

        long ipLong = ipToLong(ipStr);
        long netmask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
        long networkAddress = ipLong & netmask;
        long broadcastAddress = networkAddress | (~netmask & 0xFFFFFFFFL);

        List<String> ips = new ArrayList<>();

        if (prefixLength == 32) {
            ips.add(longToIp(networkAddress));
        } else if (prefixLength == 31) {
            ips.add(longToIp(networkAddress));
            ips.add(longToIp(broadcastAddress));
        } else {
            // Usable hosts: networkAddress + 1 to broadcastAddress - 1
            for (long i = networkAddress + 1; i < broadcastAddress; i++) {
                ips.add(longToIp(i));
            }
        }

        return ips;
    }

    public static long ipToLong(String ipAddress) {
        try {
            byte[] bytes = InetAddress.getByName(ipAddress).getAddress();
            long result = 0;
            for (byte b : bytes) {
                result = (result << 8) | (b & 0xFF);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
        }
    }

    public static String longToIp(long ipLong) {
        return String.format("%d.%d.%d.%d",
                (ipLong >> 24) & 0xFF,
                (ipLong >> 16) & 0xFF,
                (ipLong >> 8) & 0xFF,
                ipLong & 0xFF);
    }
}
