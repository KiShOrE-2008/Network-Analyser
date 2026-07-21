public class Device {
    String name, ip, type;
    boolean online = true;
    int cpu, memory, latency, packetLoss, uptime;

    public Device(String name, String ip, String type){
        this.name=name; this.ip=ip; this.type=type;
    }

    public String health(){
        if(!online || cpu>90 || packetLoss>5) return "CRITICAL";
        if(cpu>80 || memory>85 || latency>120) return "WARNING";
        return "HEALTHY";
    }

    @Override
    public String toString(){
        return name+" ("+type+") ["+ip+"] Status="+(online?"Online":"Offline")+
        " CPU="+cpu+"% MEM="+memory+"% LAT="+latency+
        "ms LOSS="+packetLoss+"% Uptime="+uptime+"h Health="+health();
    }
}
