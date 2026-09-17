package com.networkmonitor.monitoring;

import com.networkmonitor.dto.SnmpMetricDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SnmpService {

    private static final Logger log = LoggerFactory.getLogger(SnmpService.class);

    // Standard MIB OIDs
    private static final String OID_SYS_UPTIME = "1.3.6.1.2.1.1.3.0";
    private static final String OID_IF_NUMBER = "1.3.6.1.2.1.2.1.0";
    private static final String OID_HR_PROCESSOR_LOAD = "1.3.6.1.2.1.25.3.3.1.2.1"; // Host Resources MIB CPU
    private static final String OID_HR_MEMORY_SIZE = "1.3.6.1.2.1.25.2.2.0"; // Total RAM KB

    public SnmpMetricDto querySnmpMetrics(Long deviceId, String ipAddress, String community) {
        String comm = (community != null && !community.isBlank()) ? community : "public";

        Long sysUptimeSeconds = null;
        Double cpuUsagePercent = null;
        Double memoryUsagePercent = null;
        Integer interfacesCount = null;
        boolean querySuccessful = false;

        Snmp snmp = null;
        try {
            Address targetAddress = GenericAddress.parse("udp:" + ipAddress + "/161");
            if (targetAddress == null) {
                log.warn("Invalid IP address for SNMP query: {}", ipAddress);
                return createFallbackDto(deviceId, ipAddress, comm);
            }

            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget<Address> target = new CommunityTarget<>();
            target.setCommunity(new OctetString(comm));
            target.setAddress(targetAddress);
            target.setRetries(1);
            target.setTimeout(1500); // 1.5 seconds timeout
            target.setVersion(SnmpConstants.version2c);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(OID_SYS_UPTIME)));
            pdu.add(new VariableBinding(new OID(OID_IF_NUMBER)));
            pdu.add(new VariableBinding(new OID(OID_HR_PROCESSOR_LOAD)));
            pdu.add(new VariableBinding(new OID(OID_HR_MEMORY_SIZE)));
            pdu.setType(PDU.GET);

            ResponseEvent<?> response = snmp.send(pdu, target);
            if (response != null && response.getResponse() != null) {
                PDU responsePDU = response.getResponse();
                if (responsePDU.getErrorStatus() == PDU.noError) {
                    for (VariableBinding vb : responsePDU.getVariableBindings()) {
                        String oidStr = vb.getOid().toString();
                        if (oidStr.startsWith(OID_SYS_UPTIME)) {
                            sysUptimeSeconds = vb.getVariable().toLong() / 100L; // centiseconds to seconds
                            querySuccessful = true;
                        } else if (oidStr.startsWith(OID_IF_NUMBER)) {
                            interfacesCount = vb.getVariable().toInt();
                            querySuccessful = true;
                        } else if (oidStr.startsWith(OID_HR_PROCESSOR_LOAD)) {
                            cpuUsagePercent = (double) vb.getVariable().toInt();
                            querySuccessful = true;
                        } else if (oidStr.startsWith(OID_HR_MEMORY_SIZE)) {
                            long memKb = vb.getVariable().toLong();
                            if (memKb > 0) {
                                memoryUsagePercent = Math.min(100.0, (double) memKb / 1024.0 / 1024.0);
                            }
                            querySuccessful = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("SNMP query to {} failed/timed out: {}", ipAddress, e.getMessage());
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ignored) {
                }
            }
        }

        if (!querySuccessful) {
            return createFallbackDto(deviceId, ipAddress, comm);
        }

        return new SnmpMetricDto(deviceId, ipAddress, sysUptimeSeconds, cpuUsagePercent, memoryUsagePercent, interfacesCount, comm);
    }

    private SnmpMetricDto createFallbackDto(Long deviceId, String ipAddress, String community) {
        return new SnmpMetricDto(deviceId, ipAddress, null, null, null, null, community);
    }
}
