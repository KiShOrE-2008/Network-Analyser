import java.util.*;

public class Main {
    static ArrayList<Device> devices = new ArrayList<>();
    static ArrayList<String> alerts = new ArrayList<>();

    public static void dashboard(){
        int on=0,off=0,critical=0,warning=0;
        for(Device d:devices){
            if(d.online) on++; else off++;
            if(d.health().equals("CRITICAL")) critical++;
            else if(d.health().equals("WARNING")) warning++;
        }
        System.out.println("\n===== DASHBOARD =====");
        System.out.println("Devices : "+devices.size());
        System.out.println("Online  : "+on);
        System.out.println("Offline : "+off);
        System.out.println("Warning : "+warning);
        System.out.println("Critical: "+critical);
    }

    public static void main(String[] args) throws Exception{
        Scanner sc=new Scanner(System.in);
        MonitorService ms=new MonitorService();

        while(true){
            System.out.println("\n1.Add Device\n2.View Devices\n3.Start Demo Monitoring\n4.View Alerts\n5.Dashboard\n6.Exit");
            System.out.print("Choice: ");
            int ch=sc.nextInt();
            sc.nextLine();

            switch(ch){
                case 1:
                    System.out.print("Name: ");
                    String n=sc.nextLine();
                    System.out.print("IP: ");
                    String ip=sc.nextLine();
                    System.out.print("Type: ");
                    String t=sc.nextLine();
                    devices.add(new Device(n,ip,t));
                    break;

                case 2:
                    for(Device d:devices) System.out.println(d);
                    break;

                case 3:
                    if(devices.isEmpty()){
                        System.out.println("Add devices first.");
                        break;
                    }
                    for(int i=0;i<5;i++){
                        System.out.println("\n---- Scan "+(i+1)+" ----");
                        for(Device d:devices){
                            ms.update(d);
                            System.out.println(d);
                            if(d.health().equals("CRITICAL")){
                                String a="ALERT: "+d.name+" is "+d.health();
                                alerts.add(a);
                                System.out.println(a);
                            }
                        }
                        Thread.sleep(2000);
                    }
                    break;

                case 4:
                    if(alerts.isEmpty()) System.out.println("No alerts.");
                    else for(String a:alerts) System.out.println(a);
                    break;

                case 5:
                    dashboard();
                    break;

                case 6:
                    System.exit(0);
            }
        }
    }
}
