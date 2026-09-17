import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final String DEVICES_FILE = "devices.txt";
    private static final String ALERTS_FILE = "alerts.log";

    static CopyOnWriteArrayList<Device> devices = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<String> alerts = new CopyOnWriteArrayList<>();

    private static ScheduledExecutorService scheduler = null;
    private static boolean isMonitoringActive = false;

    public static void dashboard(){
        int on = 0, off = 0, critical = 0, warning = 0;
        for(Device d : devices){
            if(d.online) on++; else off++;
            if(d.health().equals("CRITICAL")) critical++;
            else if(d.health().equals("WARNING")) warning++;
        }
        System.out.println("\n\u001B[36m====================================\u001B[0m");
        System.out.println("\u001B[36m|            DASHBOARD             |\u001B[0m");
        System.out.println("\u001B[36m====================================\u001B[0m");
        System.out.printf("| Devices  : %-21d |\n", devices.size());
        System.out.printf("| Online   : \u001B[32m%-21d\u001B[0m |\n", on);
        System.out.printf("| Offline  : \u001B[31m%-21d\u001B[0m |\n", off);
        System.out.printf("| Warning  : \u001B[33m%-21d\u001B[0m |\n", warning);
        System.out.printf("| Critical : \u001B[31m%-21d\u001B[0m |\n", critical);
        System.out.printf("| BG Mon   : %-21s |\n", (isMonitoringActive ? "\u001B[32mActive\u001B[0m" : "\u001B[31mInactive\u001B[0m"));
        System.out.println("\u001B[36m====================================\u001B[0m");
    }

    private static synchronized void logAlert(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String alertLine = "[" + timestamp + "] " + message;
        alerts.add(alertLine);
        try (FileWriter fw = new FileWriter(ALERTS_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(alertLine);
        } catch (IOException e) {
            // ignore logging error
        }
    }

    private static void loadDevices() {
        File file = new File(DEVICES_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Device d = Device.fromCSV(line);
                if (d != null) {
                    devices.add(d);
                }
            }
        } catch (IOException e) {
            System.out.println("\u001B[31mError loading devices: " + e.getMessage() + "\u001B[0m");
        }
    }

    private static void saveDevices() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DEVICES_FILE))) {
            for (Device d : devices) {
                pw.println(d.toCSV());
            }
        } catch (IOException e) {
            System.out.println("\u001B[31mError saving devices: " + e.getMessage() + "\u001B[0m");
        }
    }

    private static void startBackgroundMonitoring(MonitorService ms) {
        if (isMonitoringActive) {
            System.out.println("\u001B[33mBackground monitoring is already running.\u001B[0m");
            return;
        }
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            for (Device d : devices) {
                String oldHealth = d.health();
                boolean oldOnline = d.online;
                ms.update(d);
                String newHealth = d.health();
                boolean newOnline = d.online;

                if (oldOnline != newOnline || !oldHealth.equals(newHealth)) {
                    String statusChange = String.format("%s (%s) is now %s (Health: %s)",
                        d.name,
                        d.ip,
                        newOnline ? "ONLINE" : "OFFLINE",
                        newHealth
                    );
                    logAlert("STATUS CHANGE: " + statusChange);
                    
                    // Stylized console alert printout
                    System.out.print("\n\u001B[35m[ALERT]\u001B[0m " + d.name + " went " + 
                        (newOnline ? "\u001B[32mONLINE\u001B[0m" : "\u001B[31mOFFLINE\u001B[0m") + 
                        " (Health: " + d.healthFormatted() + ")\nChoice: ");
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
        isMonitoringActive = true;
        System.out.println("\u001B[32mBackground monitoring started (Scans every 10 seconds).\u001B[0m");
    }

    private static void stopBackgroundMonitoring() {
        if (!isMonitoringActive || scheduler == null) {
            System.out.println("\u001B[33mBackground monitoring is not running.\u001B[0m");
            return;
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        isMonitoringActive = false;
        System.out.println("\u001B[32mBackground monitoring stopped.\u001B[0m");
    }

    private static boolean isValidIpOrHostname(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        ip = ip.trim();

        String ipv4Pattern = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        String ipv6Pattern = "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
                             "^((?:[0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4})?::((?:[0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4})?$";
        String hostnamePattern = "^(?=.{1,253}$)(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)*[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?$";

        if (ip.matches("^[0-9.]+$")) {
            return ip.matches(ipv4Pattern);
        }
        return ip.matches(ipv4Pattern) || ip.matches(ipv6Pattern) || ip.matches(hostnamePattern);
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        MonitorService ms = new MonitorService();

        loadDevices();

        while(true){
            System.out.println("\n\u001B[34m--- NETWORK DEVICE MONITOR ---\u001B[0m");
            System.out.println("1. Add Device");
            System.out.println("2. View Devices");
            System.out.println("3. Delete Device");
            System.out.println("4. " + (isMonitoringActive ? "\u001B[31mStop Background Monitoring\u001B[0m" : "\u001B[32mStart Background Monitoring\u001B[0m"));
            System.out.println("5. View Alerts");
            System.out.println("6. Dashboard");
            System.out.println("7. Exit");
            System.out.print("\u001B[33mChoice: \u001B[0m");
            
            if(!sc.hasNextInt()) {
                System.out.println("\u001B[31mInvalid choice. Please enter a number.\u001B[0m");
                sc.nextLine();
                continue;
            }
            int ch = sc.nextInt();
            sc.nextLine();

            switch(ch){
                case 1:
                    System.out.print("Name: ");
                    String n = sc.nextLine();
                    if (n.trim().isEmpty()) {
                        System.out.println("\u001B[31mName cannot be empty. Operation cancelled.\u001B[0m");
                        break;
                    }
                    String ip = "";
                    while (true) {
                        System.out.print("IP/Hostname: ");
                        ip = sc.nextLine().trim();
                        if (ip.isEmpty()) {
                            System.out.println("\u001B[31mIP/Hostname cannot be empty.\u001B[0m");
                            continue;
                        }
                        if (ip.equalsIgnoreCase("cancel")) {
                            System.out.println("\u001B[33mOperation cancelled.\u001B[0m");
                            ip = null;
                            break;
                        }
                        if (isValidIpOrHostname(ip)) {
                            break;
                        } else {
                            System.out.println("\u001B[31mInvalid IP/Hostname format. Please enter a valid IPv4, IPv6, or domain name (or type 'cancel').\u001B[0m");
                        }
                    }
                    if (ip == null) {
                        break;
                    }
                    System.out.print("Type: ");
                    String t = sc.nextLine();
                    devices.add(new Device(n, ip, t));
                    saveDevices();
                    System.out.println("\u001B[32mDevice added and persisted.\u001B[0m");
                    break;

                case 2:
                    if(devices.isEmpty()){
                        System.out.println("\u001B[33mNo devices registered.\u001B[0m");
                    } else {
                        System.out.println("\n--- Registered Devices ---");
                        for(Device d : devices) System.out.println(d);
                    }
                    break;

                case 3:
                    if(devices.isEmpty()){
                        System.out.println("\u001B[33mNo devices to delete.\u001B[0m");
                        break;
                    }
                    System.out.println("\nSelect a device to delete:");
                    for(int i = 0; i < devices.size(); i++){
                        System.out.println((i+1) + ". " + devices.get(i).name + " [" + devices.get(i).ip + "]");
                    }
                    System.out.print("\u001B[33mChoice: \u001B[0m");
                    if(sc.hasNextInt()){
                        int index = sc.nextInt() - 1;
                        sc.nextLine();
                        if(index >= 0 && index < devices.size()){
                            Device removed = devices.remove(index);
                            saveDevices();
                            System.out.println("\u001B[32mRemoved device: " + removed.name + "\u001B[0m");
                        } else {
                            System.out.println("\u001B[31mInvalid selection.\u001B[0m");
                        }
                    } else {
                        System.out.println("\u001B[31mInvalid input.\u001B[0m");
                        sc.nextLine();
                    }
                    break;

                case 4:
                    if (isMonitoringActive) {
                        stopBackgroundMonitoring();
                    } else {
                        if (devices.isEmpty()) {
                            System.out.println("\u001B[33mAdd devices first before starting monitoring.\u001B[0m");
                        } else {
                            startBackgroundMonitoring(ms);
                        }
                    }
                    break;

                case 5:
                    if(alerts.isEmpty()) {
                        System.out.println("\u001B[32mNo alerts recorded.\u001B[0m");
                    } else {
                        System.out.println("\n--- Alerts History (logged to alerts.log) ---");
                        for(String a : alerts) {
                            if (a.contains("OFFLINE") || a.contains("CRITICAL")) {
                                System.out.println("\u001B[31m" + a + "\u001B[0m");
                            } else if (a.contains("WARNING")) {
                                System.out.println("\u001B[33m" + a + "\u001B[0m");
                            } else {
                                System.out.println("\u001B[32m" + a + "\u001B[0m");
                            }
                        }
                    }
                    break;

                case 6:
                    dashboard();
                    break;

                case 7:
                    if (isMonitoringActive && scheduler != null) {
                        scheduler.shutdownNow();
                    }
                    System.out.println("\u001B[32mExiting. Goodbye!\u001B[0m");
                    System.exit(0);

                default:
                    System.out.println("\u001B[31mInvalid option.\u001B[0m");
            }
        }
    }
}
