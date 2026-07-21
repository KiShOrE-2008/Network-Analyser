import java.util.*;

public class MonitorService {
    Random r = new Random();

    public void update(Device d){
        d.online = r.nextInt(100) > 5;
        d.cpu = r.nextInt(101);
        d.memory = r.nextInt(101);
        d.latency = 5 + r.nextInt(196);
        d.packetLoss = r.nextInt(11);
        d.uptime += 1;
    }
}
