package com.networkmonitor.monitoring;

import com.networkmonitor.dto.NmapHostResultDto;
import com.networkmonitor.dto.NmapPortResultDto;
import com.networkmonitor.dto.NmapScanResultDto;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class NmapService {

    public boolean isNmapAvailable() {
        try {
            Process process = new ProcessBuilder("nmap", "--version").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public NmapScanResultDto scanTarget(String target, String scanProfile) {
        long startTime = System.currentTimeMillis();
        NmapScanResultDto result = new NmapScanResultDto();
        result.setTarget(target);
        result.setScanProfile(scanProfile != null ? scanProfile : "FAST_PORT");

        boolean available = isNmapAvailable();
        result.setNmapAvailable(available);

        if (!available) {
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return result;
        }

        List<String> command = new ArrayList<>();
        command.add("nmap");

        if ("HOST_DISCOVERY".equalsIgnoreCase(scanProfile)) {
            command.add("-sn");
        } else if ("DETAILED".equalsIgnoreCase(scanProfile)) {
            command.add("-F");
            command.add("-sV");
        } else {
            // Default FAST_PORT scan
            command.add("-F");
        }

        command.add("-oX");
        command.add("-");
        command.add(target);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            InputStream is = process.getInputStream();
            byte[] xmlBytes = is.readAllBytes();
            process.waitFor();

            if (xmlBytes.length > 0) {
                parseNmapXml(new ByteArrayInputStream(xmlBytes), result);
            }
        } catch (Exception e) {
            result.setNmapAvailable(false);
        }

        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        return result;
    }

    public void parseNmapXml(InputStream xmlInputStream, NmapScanResultDto resultDto) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Disable DTD validation for security
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlInputStream);
            doc.getDocumentElement().normalize();

            NodeList hostList = doc.getElementsByTagName("host");
            int totalHosts = hostList.getLength();
            int hostsUp = 0;

            for (int i = 0; i < hostList.getLength(); i++) {
                Element hostElem = (Element) hostList.item(i);

                // Status
                String state = "unknown";
                NodeList statusList = hostElem.getElementsByTagName("status");
                if (statusList.getLength() > 0) {
                    Element statusElem = (Element) statusList.item(0);
                    state = statusElem.getAttribute("state");
                }

                if ("up".equalsIgnoreCase(state)) {
                    hostsUp++;
                }

                // IP Address
                String ipAddress = null;
                NodeList addrList = hostElem.getElementsByTagName("address");
                for (int j = 0; j < addrList.getLength(); j++) {
                    Element addrElem = (Element) addrList.item(j);
                    if ("ipv4".equalsIgnoreCase(addrElem.getAttribute("addrtype")) || ipAddress == null) {
                        ipAddress = addrElem.getAttribute("addr");
                    }
                }

                if (ipAddress == null) continue;

                // Hostname
                String hostname = ipAddress;
                NodeList hostnameList = hostElem.getElementsByTagName("hostname");
                if (hostnameList.getLength() > 0) {
                    Element hNameElem = (Element) hostnameList.item(0);
                    hostname = hNameElem.getAttribute("name");
                }

                // OS Match
                String osMatch = "Unknown OS";
                NodeList osMatchList = hostElem.getElementsByTagName("osmatch");
                if (osMatchList.getLength() > 0) {
                    Element osElem = (Element) osMatchList.item(0);
                    osMatch = osElem.getAttribute("name");
                }

                NmapHostResultDto hostDto = new NmapHostResultDto(ipAddress, state, hostname, osMatch);

                // Open Ports
                NodeList portList = hostElem.getElementsByTagName("port");
                for (int k = 0; k < portList.getLength(); k++) {
                    Element portElem = (Element) portList.item(k);
                    String protocol = portElem.getAttribute("protocol");
                    int portId = Integer.parseInt(portElem.getAttribute("portid"));

                    String portState = "closed";
                    NodeList pStateList = portElem.getElementsByTagName("state");
                    if (pStateList.getLength() > 0) {
                        portState = ((Element) pStateList.item(0)).getAttribute("state");
                    }

                    if ("open".equalsIgnoreCase(portState)) {
                        String serviceName = "unknown";
                        String product = "";
                        String version = "";

                        NodeList serviceList = portElem.getElementsByTagName("service");
                        if (serviceList.getLength() > 0) {
                            Element serviceElem = (Element) serviceList.item(0);
                            serviceName = serviceElem.getAttribute("name");
                            product = serviceElem.getAttribute("product");
                            version = serviceElem.getAttribute("version");
                        }

                        hostDto.getOpenPorts().add(new NmapPortResultDto(
                                portId, protocol, portState, serviceName, product, version
                        ));
                    }
                }

                resultDto.getHosts().add(hostDto);
            }

            resultDto.setTotalHostsScanned(totalHosts);
            resultDto.setHostsUpCount(hostsUp);

        } catch (Exception e) {
            // Keep gracefully empty list on parse error
        }
    }
}
