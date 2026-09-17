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
import java.util.ArrayList;
import java.util.List;

@Service
public class NmapService {
    public boolean isNmapAvailable() {
        try {
            Process process = new ProcessBuilder("nmap", "--version").start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public NmapScanResultDto scanTarget(String target, String scanProfile) {
        long start = System.currentTimeMillis();
        NmapScanResultDto result = new NmapScanResultDto();
        result.setTarget(target);
        result.setScanProfile(scanProfile != null ? scanProfile : "FAST_PORT");
        boolean available = isNmapAvailable();
        result.setNmapAvailable(available);
        if (!available) { result.setExecutionTimeMs(System.currentTimeMillis() - start); return result; }

        List<String> command = new ArrayList<>();
        command.add("nmap");
        if ("HOST_DISCOVERY".equalsIgnoreCase(scanProfile)) {
            command.add("-sn");
        } else if ("DETAILED".equalsIgnoreCase(scanProfile)) {
            command.add("-F");
            command.add("-sV");
        } else {
            command.add("-F");
        }
        command.add("-oX"); command.add("-"); command.add(target);

        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            byte[] xmlBytes = process.getInputStream().readAllBytes();
            process.waitFor();
            if (xmlBytes.length > 0) parseNmapXml(new ByteArrayInputStream(xmlBytes), result);
        } catch (Exception e) {
            result.setNmapAvailable(false);
        }
        result.setExecutionTimeMs(System.currentTimeMillis() - start);
        return result;
    }

    public void parseNmapXml(InputStream xmlInputStream, NmapScanResultDto resultDto) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlInputStream);
            doc.getDocumentElement().normalize();

            NodeList hostList = doc.getElementsByTagName("host");
            int hostsUp = 0;
            for (int i = 0; i < hostList.getLength(); i++) {
                Element host = (Element) hostList.item(i);
                String state = "unknown";
                NodeList status = host.getElementsByTagName("status");
                if (status.getLength() > 0) state = ((Element) status.item(0)).getAttribute("state");
                if ("up".equalsIgnoreCase(state)) hostsUp++;

                String ip = null, mac = null, vendor = null;
                NodeList addresses = host.getElementsByTagName("address");
                for (int j = 0; j < addresses.getLength(); j++) {
                    Element a = (Element) addresses.item(j);
                    String type = a.getAttribute("addrtype");
                    if ("ipv4".equalsIgnoreCase(type)) ip = a.getAttribute("addr");
                    else if ("mac".equalsIgnoreCase(type)) { mac = a.getAttribute("addr"); vendor = a.getAttribute("vendor"); }
                    else if (ip == null) ip = a.getAttribute("addr");
                }
                if (ip == null || ip.isBlank()) continue;

                String hostname = ip;
                NodeList names = host.getElementsByTagName("hostname");
                if (names.getLength() > 0) hostname = ((Element) names.item(0)).getAttribute("name");
                String os = "Unknown OS";
                NodeList osMatches = host.getElementsByTagName("osmatch");
                if (osMatches.getLength() > 0) os = ((Element) osMatches.item(0)).getAttribute("name");

                NmapHostResultDto dto = new NmapHostResultDto(ip, state, hostname, os);
                dto.setMacAddress(mac); dto.setVendor(vendor);

                NodeList ports = host.getElementsByTagName("port");
                for (int k = 0; k < ports.getLength(); k++) {
                    Element p = (Element) ports.item(k);
                    NodeList states = p.getElementsByTagName("state");
                    String portState = states.getLength() > 0 ? ((Element) states.item(0)).getAttribute("state") : "closed";
                    if (!"open".equalsIgnoreCase(portState)) continue;
                    int port = Integer.parseInt(p.getAttribute("portid"));
                    String protocol = p.getAttribute("protocol"), service = "unknown", product = "", version = "";
                    NodeList services = p.getElementsByTagName("service");
                    if (services.getLength() > 0) {
                        Element s = (Element) services.item(0);
                        service = s.getAttribute("name"); product = s.getAttribute("product"); version = s.getAttribute("version");
                    }
                    dto.getOpenPorts().add(new NmapPortResultDto(port, protocol, portState, service, product, version));
                }
                resultDto.getHosts().add(dto);
            }
            resultDto.setTotalHostsScanned(hostList.getLength());
            resultDto.setHostsUpCount(hostsUp);
        } catch (Exception ignored) {
            // Discovery remains usable through ping if Nmap output cannot be parsed.
        }
    }
}
