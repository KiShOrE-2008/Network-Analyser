# Network Device Monitoring System (Demo)

A lightweight, multi-threaded, console-based Java application designed to actively monitor network devices, track their performance metrics (CPU, Memory, Latency, and Packet Loss), verify port availability, log real-time status alerts, and display an interactive dashboard.

## Key Features

- **Interactive CLI & Dashboard**: View summary metrics including device counts, online/offline count, warnings, critical issues, and background monitoring status.
- **Active Network Monitoring**:
  - **ICMP Ping Verification**: Verifies online/offline state using active ICMP ping commands with an 800ms timeout.
  - **TCP Port Scanning**: Checks for service availability across common ports: SSH (`22`), HTTP (`80`), HTTPS (`443`), Web/Proxy (`8080`), and RDP (`3389`).
- **Dynamic Metrics Simulation**: Emulates fluctuating resource metrics (CPU, memory, packet loss, and uptime) for active devices.
- **Background Scheduling**: Runs automated device scans periodically in the background (every 10 seconds) using a thread-pool executor.
- **Real-Time Alert Logging**: Detects changes in device health or online status, outputs colored console alerts, and records persistent timestamps to [alerts.log](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/alerts.log).
- **Persistent Storage**: Save/load configuration in a simple CSV flat-file, [devices.txt](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/devices.txt).
- **Rich Terminal Styling**: Displays metrics and alerts in color-coded text (Red for Critical, Yellow for Warning, Green for Healthy) using ANSI escape codes.

---

## File Structure

The project is composed of the following key files:
- [Main.java](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/Main.java): Houses the entry point, the main interactive menu loop, background monitoring scheduler control, and console UI dashboard logic.
- [Device.java](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/Device.java): The data model representing configured network devices, containing serialization helper methods (`toCSV`, `fromCSV`), metrics storage, and health-checking status evaluation.
- [MonitorService.java](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/MonitorService.java): The monitoring worker containing network reachable ping routines, port scan logic, and metric generation/simulation code.
- [devices.txt](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/devices.txt): Text-based storage for persistence of configured devices.
- [alerts.log](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/alerts.log): Text file storing historical alerts with timestamps.

---

## Health Rules & Severity Thresholds

The monitoring service classifies device states into three severity tiers:

| Severity | Color | Conditions |
| :--- | :--- | :--- |
| **`CRITICAL`** | Red | Device is Offline, CPU usage > 90%, or Packet Loss > 5% |
| **`WARNING`** | Yellow | CPU usage > 80%, Memory usage > 85%, or Latency > 120ms |
| **`HEALTHY`** | Green | Active operations within acceptable limits |

---

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher installed on your system.

### Compilation

Compile the source code files:

```bash
javac Main.java Device.java MonitorService.java
```

### Running the Application

Launch the application using:

```bash
java Main
```

---

## Interactive Menu Options

1. **Add Device**: Register a new device to be monitored.
   - **Input Validation**: The IP/Hostname input is strictly validated (supporting IPv4, IPv6, and domain names). If the input format is invalid, you will be prompted to re-enter it or type `cancel` to abort.
   - **Fields**: Name (cannot be empty), IP/Hostname, and Type (e.g. router, pc, server).
2. **View Devices**: Display details of all registered devices, including real-time performance indicators (Uptime, CPU, Memory, Latency, Packet Loss, Open Ports, and Health status).
3. **Delete Device**: Remove a registered device from the monitoring list interactively.
4. **Start/Stop Background Monitoring**: Toggle the periodic execution of the monitoring service (performs active ICMP pinging & port scans every 10 seconds).
5. **View Alerts**: Display the historic logs of status changes (Healthy/Warning/Critical transitions) colored by severity.
6. **Dashboard**: View overall statistics about system status and metrics.
7. **Exit**: Terminate background threads and safely quit the application.
