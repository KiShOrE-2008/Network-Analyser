package com.networkmonitor.monitoring;

import com.networkmonitor.dto.NmapScanResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class NmapServiceTest {

    private NmapService nmapService;

    @BeforeEach
    void setUp() {
        nmapService = new NmapService();
    }

    @Test
    @DisplayName("isNmapAvailable should return true when Nmap binary is installed")
    void isNmapAvailable_ShouldReturnTrue() {
        boolean available = nmapService.isNmapAvailable();
        assertThat(available).isTrue();
    }

    @Test
    @DisplayName("parseNmapXml should accurately extract host IP, state, hostname, and open ports")
    void parseNmapXml_ShouldParseXmlStructure() {
        String sampleXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<nmaprun scanner=\"nmap\" version=\"7.99\">\n" +
                "  <host>\n" +
                "    <status state=\"up\"/>\n" +
                "    <address addr=\"192.168.1.1\" addrtype=\"ipv4\"/>\n" +
                "    <hostnames><hostname name=\"router.local\" type=\"PTR\"/></hostnames>\n" +
                "    <ports>\n" +
                "      <port protocol=\"tcp\" portid=\"80\">\n" +
                "        <state state=\"open\"/>\n" +
                "        <service name=\"http\" product=\"Apache httpd\" version=\"2.4.52\"/>\n" +
                "      </port>\n" +
                "      <port protocol=\"tcp\" portid=\"22\">\n" +
                "        <state state=\"open\"/>\n" +
                "        <service name=\"ssh\" product=\"OpenSSH\" version=\"8.9\"/>\n" +
                "      </port>\n" +
                "    </ports>\n" +
                "  </host>\n" +
                "</nmaprun>";

        InputStream inputStream = new ByteArrayInputStream(sampleXml.getBytes(StandardCharsets.UTF_8));
        NmapScanResultDto result = new NmapScanResultDto();
        result.setTarget("192.168.1.1");

        nmapService.parseNmapXml(inputStream, result);

        assertThat(result.getTotalHostsScanned()).isEqualTo(1);
        assertThat(result.getHostsUpCount()).isEqualTo(1);
        assertThat(result.getHosts()).hasSize(1);

        var host = result.getHosts().get(0);
        assertThat(host.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(host.getHostname()).isEqualTo("router.local");
        assertThat(host.getStatus()).isEqualTo("up");
        assertThat(host.getOpenPorts()).hasSize(2);

        var port80 = host.getOpenPorts().stream().filter(p -> p.getPort() == 80).findFirst().orElse(null);
        assertThat(port80).isNotNull();
        assertThat(port80.getServiceName()).isEqualTo("http");
        assertThat(port80.getServiceProduct()).isEqualTo("Apache httpd");
        assertThat(port80.getServiceVersion()).isEqualTo("2.4.52");
    }

    @Test
    @DisplayName("scanTarget on loopback (127.0.0.1) should execute real nmap scan")
    void scanTarget_Loopback_ShouldReturnHosts() {
        NmapScanResultDto result = nmapService.scanTarget("127.0.0.1", "FAST_PORT");

        assertThat(result).isNotNull();
        assertThat(result.isNmapAvailable()).isTrue();
        assertThat(result.getHosts()).isNotEmpty();
        assertThat(result.getHosts().get(0).getIpAddress()).isEqualTo("127.0.0.1");
    }
}
