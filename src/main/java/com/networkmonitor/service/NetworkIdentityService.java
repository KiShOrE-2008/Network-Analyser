package com.networkmonitor.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NetworkIdentityService {
    private static final Pattern MAC = Pattern.compile("(?i)([0-9a-f]{2}(?::|-)){5}[0-9a-f]{2}");
    private static final Map<String,String> COMMON_OUIS = new HashMap<>();
    static {
        COMMON_OUIS.put("B827EB", "Raspberry Pi"); COMMON_OUIS.put("DC4A3E", "Google");
        COMMON_OUIS.put("3C5A37", "Google"); COMMON_OUIS.put("F4F5D8", "TP-Link");
        COMMON_OUIS.put("50C7BF", "TP-Link"); COMMON_OUIS.put("B0487A", "TP-Link");
        COMMON_OUIS.put("FCFBFB", "Xiaomi"); COMMON_OUIS.put("F0B429", "Xiaomi");
        COMMON_OUIS.put("8C8590", "Samsung"); COMMON_OUIS.put("A4D18C", "Samsung");
        COMMON_OUIS.put("3C2EF9", "Apple"); COMMON_OUIS.put("A8BBCF", "Apple");
        COMMON_OUIS.put("D850E6", "Apple"); COMMON_OUIS.put("001C42", "Dell");
        COMMON_OUIS.put("3C970E", "Dell"); COMMON_OUIS.put("FC3497", "Dell");
        COMMON_OUIS.put("001B21", "Intel"); COMMON_OUIS.put("3C970E", "Dell");
        COMMON_OUIS.put("E8B2AC", "Realtek"); COMMON_OUIS.put("001E68", "Wistron");
    }

    public Identity lookup(String ip) {
        String mac = lookupArpMac(ip);
        String vendor = vendorFor(mac);
        return new Identity(mac, vendor);
    }

    private String lookupArpMac(String ip) {
        String[] commands = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")
                ? new String[]{"arp", "-a", ip} : new String[]{"ip", "neigh", "show", ip};
        try {
            Process p = new ProcessBuilder(commands).redirectErrorStream(true).start();
            StringBuilder out = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line; while ((line=r.readLine()) != null) out.append(line).append('\n');
            }
            p.waitFor();
            Matcher m=MAC.matcher(out.toString());
            if(m.find()) return m.group().replace('-', ':').toUpperCase(Locale.ROOT);
        } catch(Exception ignored) {}
        return null;
    }

    private String vendorFor(String mac) {
        if(mac==null) return null;
        String key=mac.replace(":","").replace("-","").substring(0,6).toUpperCase(Locale.ROOT);
        return COMMON_OUIS.getOrDefault(key, "Unknown");
    }

    public static final class Identity {
        private final String macAddress; private final String vendor;
        public Identity(String macAddress,String vendor){this.macAddress=macAddress;this.vendor=vendor;}
        public String getMacAddress(){return macAddress;} public String getVendor(){return vendor;}
    }
}
