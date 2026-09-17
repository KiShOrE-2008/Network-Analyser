# NetScope — Automatic Network Discovery & Monitoring

NetScope is a Java 21 / Spring Boot application that discovers devices on the **local network automatically**, imports them into PostgreSQL, measures reachability, and presents the observed information in a web dashboard.

## What the application does

1. Detects usable IPv4 interfaces and calculates their local CIDR networks.
2. Scans suitable local subnets concurrently for reachable hosts.
3. Automatically imports discovered hosts into the device inventory.
4. Enriches discovered devices with Nmap information when Nmap is installed and usable:
   - IP address
   - hostname
   - MAC address (when visible on the local L2 network)
   - vendor (when Nmap provides an OUI vendor)
   - discovered open TCP ports and service/version information
   - OS fingerprint when the selected Nmap scan provides one
5. Continuously monitors imported devices with real reachability/latency checks.
6. Stores ping history, device state events and alerts in PostgreSQL.
7. Exposes SNMP queries for devices that actually provide SNMP telemetry.
8. Provides a responsive NetScope dashboard for discovery, inventory, topology, history and alerts.

> **Important:** A remote machine cannot reveal arbitrary CPU/RAM/OS information simply because it is on the same network. Values are only shown when the target exposes them through a supported mechanism such as SNMP or Nmap. Otherwise the dashboard reports the value as unavailable rather than inventing it.

## Technology

- Java 21
- Spring Boot 3.4.2
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Nmap
- SNMP4J
- ICMP/TCP reachability checks
- HTML/CSS/JavaScript SPA
- Spring WebSocket / STOMP support
- Maven
- Docker / Docker Compose

## Automatic discovery flow

```text
Application starts
       |
       v
Detect local IPv4 interfaces
       |
       v
Calculate attached CIDR subnet(s)
       |
       v
Concurrent reachability scan
       |
       +---- Nmap available? ---- yes ---> Nmap enrichment
       |                                  (MAC/vendor/services)
       v
Import / update device inventory
       |
       v
Continuous monitoring
       |
       +--> Ping + latency + packet loss
       +--> State changes + alerts
       +--> History in PostgreSQL
       +--> SNMP telemetry when supported
       |
       v
NetScope web dashboard
```

## REST endpoints

### Discovery

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/discovery/local-networks` | Show detected local IPv4 networks |
| POST | `/api/discovery/auto` | Scan local networks and automatically import/update devices |
| GET | `/api/discovery/auto/status` | Show automatic discovery state |
| POST | `/api/discovery/scan` | Scan an explicitly supplied CIDR |
| GET | `/api/discovery/nmap/status` | Check Nmap availability |
| POST | `/api/discovery/nmap` | Run an Nmap subnet scan |

### Devices & monitoring

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/devices` | Device inventory/search/filter |
| GET | `/api/devices/{id}` | Device details |
| POST | `/api/devices/{id}/check` | Real reachability/latency check |
| GET | `/api/devices/{id}/metrics` | Ping history |
| GET | `/api/devices/{id}/events` | Device state events |
| GET | `/api/devices/{id}/ports` | Stored port status |
| POST | `/api/devices/{id}/scan-ports` | TCP port scan |
| POST | `/api/devices/{id}/nmap-scan` | Nmap scan for one device |
| GET | `/api/devices/{id}/snmp` | Query SNMP telemetry |

### History, alerts and reports

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/history/device/{id}` | Historical device metrics |
| GET | `/api/events` | Recent system/device events |
| GET | `/api/alerts` | Alert history |
| PATCH | `/api/alerts/{id}/resolve` | Resolve an alert |
| GET | `/api/reports/summary` | System monitoring summary |
| GET | `/api/reports/export/csv` | Export device inventory CSV |

## Run locally

Prerequisites:

- JDK 21
- PostgreSQL 16
- Nmap (recommended for MAC/vendor/service enrichment)
- Maven 3.9+

```bash
./mvnw clean test
./mvnw spring-boot:run
```

Then open:

```text
http://localhost:8080
```

The first automatic discovery cycle runs after the configured initial delay. You can also press **Start discovery** in the NetScope dashboard.

## Docker on Linux

For real LAN discovery from a Dockerized application, the app container uses host networking. This is intentional: normal Docker bridge networking would expose the container's private Docker subnet instead of the host's actual LAN.

```bash
docker compose up --build -d
```

Then open `http://localhost:8080`.

To stop:

```bash
docker compose down
```

## Configuration

Environment variables:

```text
SPRING_DATASOURCE_URL
DB_USERNAME
DB_PASSWORD
DISCOVERY_INITIAL_DELAY
DISCOVERY_INTERVAL
MONITORING_INTERVAL
SPRING_JPA_HIBERNATE_DDL_AUTO
```

The application only automatically scans networks attached to the machine running the application and applies a bounded host-count limit to automatic discovery.

## Verification

A GitHub Actions workflow is included to run the Maven test suite against PostgreSQL on pushes and pull requests. The repository should be treated as verified only after the CI run reports success; local runtime behavior also depends on the machine's network interfaces, permissions, Nmap installation and target-device protocols.
