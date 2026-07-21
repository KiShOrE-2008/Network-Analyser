import java.util.ArrayList;
import java.util.List;

public class Device {
    public String name, ip, type;
    public boolean online = true;
    public int cpu, memory, latency, packetLoss, uptime;
    public List<Integer> openPorts = new ArrayList<>();

    public Device(String name, String ip, String type){
        this.name = name;
        this.ip = ip;
        this.type = type;
    }

    public String health(){
        if(!online || cpu > 90 || packetLoss > 5) return "CRITICAL";
        if(cpu > 80 || memory > 85 || latency > 120) return "WARNING";
        return "HEALTHY";
    }

    public String healthFormatted() {
        String h = health();
        if(h.equals("CRITICAL")) return "\u001B[31mCRITICAL\u001B[0m";
        if(h.equals("WARNING")) return "\u001B[33mWARNING\u001B[0m";
        return "\u001B[32mHEALTHY\u001B[0m";
    }

    public String toCSV() {
        return name + "," + ip + "," + type;
    }

    public static Device fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if(parts.length >= 3) {
            return new Device(parts[0].trim(), parts[1].trim(), parts[2].trim());
        }
        return null;
    }

    @Override
    public String toString(){
        String statusStr = online ? "\u001B[32mOnline\u001B[0m" : "\u001B[31mOffline\u001B[0m";
        return "\u001B[1m" + name + "\u001B[0m (" + type + ") [" + ip + "] " +
               "Status=" + statusStr +
               " CPU=" + cpu + "%" +
               " MEM=" + memory + "%" +
               " LAT=" + latency + "ms" +
               " LOSS=" + packetLoss + "%" +
               " Uptime=" + uptime + "h" +
               " Ports=" + openPorts +
               " Health=" + healthFormatted();
    }
}

