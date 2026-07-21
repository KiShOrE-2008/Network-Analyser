# Network Device Monitoring System (Demo)

A lightweight, console-based Java application designed to monitor network devices, track their metrics (CPU, Memory usage, Latency, and Packet Loss), display real-time status alerts, and provide an interactive dashboard.

## Features

- **Add Device**: Add a device to be monitored by specifying its name, IP address, and type.
- **View Devices**: List all configured devices with their current health and status metrics.
- **Delete Device**: Interactively remove a device from the monitor list.
- **Start Demo Monitoring**: Run a live simulation that updates device metrics periodically and reports alerts.
- **View Alerts**: Check the historical log of critical issues (e.g., offline status, high CPU usage, or high packet loss).
- **Dashboard**: Get a quick overview of the total number of devices, online/offline count, and warning/critical states.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher.

### Compilation

Compile the Java source files using:

```bash
javac *.java
```

### Running the Application

Execute the main program with:

```bash
java Main
```

## How It Works

- **Simulation**: The application utilizes `MonitorService` to simulate fluctuating metric values (CPU, memory, latency, packet loss, uptime).
- **Health Rules**:
  - `CRITICAL`: Offline, CPU > 90%, or Packet Loss > 5%.
  - `WARNING`: CPU > 80%, Memory > 85%, or Latency > 120ms.
  - `HEALTHY`: Normal operations.
