import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonitorService {
    private final Random r = new Random();
    private final int[] portsToScan = {22, 80, 443, 8080, 3389};

    public void update(Device d) {
        boolean reachable = false;
        long latencyMs = 0;

        // Try ICMP Ping
        try {
            long start = System.currentTimeMillis();
            InetAddress address = InetAddress.getByName(d.ip);
            reachable = address.isReachable(800); // 800ms timeout
            long end = System.currentTimeMillis();
            if (reachable) {
                latencyMs = end - start;
            }
        } catch (IOException e) {
            // host not found or other network issue
        }

        // Try TCP Port Scan (also detects online state if ICMP is blocked)
        List<Integer> openPorts = new ArrayList<>();
        boolean tcpOnline = false;
        long tcpStart = System.currentTimeMillis();
        for (int port : portsToScan) {
            try {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(d.ip, port), 200); // 200ms timeout
                    openPorts.add(port);
                    tcpOnline = true;
                }
            } catch (IOException e) {
                // port closed
            }
        }
        long tcpEnd = System.currentTimeMillis();

        if (tcpOnline && !reachable) {
            reachable = true;
            latencyMs = (tcpEnd - tcpStart) / openPorts.size(); // approximate latency
        }

        d.openPorts = openPorts;

        if (reachable) {
            d.online = true;
            d.latency = (int) latencyMs;
            d.packetLoss = r.nextInt(3); // low random packet loss (0-2%) when online
            d.cpu = r.nextInt(101);
            d.memory = r.nextInt(101);
            d.uptime += 1;
        } else {
            // Device is offline
            d.online = false;
            d.cpu = 0;
            d.memory = 0;
            d.latency = 0;
            d.packetLoss = 100;
            d.openPorts.clear();
        }
    }
}

