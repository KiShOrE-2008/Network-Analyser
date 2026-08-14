# Enterprise Network Device Monitoring System

An enterprise-grade, high-performance Network Device Monitoring and Discovery backend built with **Java 21**, **Spring Boot 3.4.2**, and **PostgreSQL**.

The system provides real-time active network polling (ICMP Ping, TCP Port Scanning, SNMP metric extraction, Nmap discovery), an intelligent health rule engine with alerting, a RESTful API backend, and WebSocket integration for dynamic web dashboards.

---

## 🚀 Current Project Status

- ✅ **PHASE 0**: Requirements & Architecture frozen ([ARCHITECTURE.md](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/ARCHITECTURE.md)).
- ✅ **PHASE 1**: Spring Boot 3.4.2 + PostgreSQL Integration with core `Device` JPA Entity.
- ✅ **PHASE 2**: Complete Device Management REST APIs, validation engine (IPv4 Regex & octet bounds), JPA Specifications search/filtering.
- ✅ **PHASE 3**: Real Ping / ICMP Monitoring Engine (OS ping process parsing, latency measurement, packet loss %, metric persistence in `monitoring_metrics`).
- ✅ **PHASE 4**: TCP Port Monitoring (Socket connection scanning for standard services: SSH, DNS, HTTP, HTTPS, SMB, MySQL, PostgreSQL, RDP, HTTP-Alt; persistence in `port_status`).
- ✅ **PHASE 5**: Automatic Network Subnet Discovery (CIDR range parsing `/16` to `/32`, pluggable strategy pattern, concurrent multithreaded range scan, mass device import).
- ✅ **PHASE 6**: Advanced Nmap Integration (System Nmap 7.99 process execution, XML DOM parser for ports, software versions, and OS fingerprinting).
- ✅ **PHASE 7**: Automatic Bounded Monitoring Scheduler Engine (Background `@Scheduled` worker pool `10` threads, dynamic start/stop REST endpoints).
- ✅ **PHASE 8**: Health Engine & State-Change Alerting (`HEALTHY`, `WARNING`, `CRITICAL` state rule analysis, alert deduplication, recovery auto-resolution, persistence in `alerts`).
- ⏳ **PHASE 9 & 10** *(Next)*: Web Dashboard (React) & Real-Time WebSockets.

---

## 🛠️ Technology Stack

| Component | Technology | Description |
|---|---|---|
| **Backend Framework** | Java 21 / Spring Boot 3.4.2 | Core service layer, REST APIs, & async task processing |
| **Database** | PostgreSQL 16+ | Relational persistence for devices, metrics, ports, and alerts |
| **ORM / Persistence** | Spring Data JPA / Hibernate | Entity mapping & transactional persistence |
| **Network Tools** | Native ICMP Ping, Sockets, Nmap 7.99 | Network reachability, port scanning, & OS fingerprinting |
| **Scheduler & Worker Pool** | Spring `@Scheduled` / `FixedThreadPool` | Bounded thread pool worker engine |
| **Health & Alerting** | Rule Engine & Deduplication | State transition alerts & auto-recovery resolution |
| **Build & Testing** | Maven 3.9+ / JUnit 5 / Mockito | Package management & 53 automated tests |
| **Architecture Doc** | [ARCHITECTURE.md](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/ARCHITECTURE.md) | Mermaid diagrams for system flow, components, and ERD |

---

## 🗺️ Project Roadmap & Milestones

```text
PHASE 0  → Requirements + Architecture [COMPLETED]
PHASE 1  → Java/Spring Boot + PostgreSQL [COMPLETED]
PHASE 2  → Device Management APIs [COMPLETED]
PHASE 3  → Real Ping/ICMP Monitoring [COMPLETED]
PHASE 4  → TCP Port Monitoring [COMPLETED]
PHASE 5  → Subnet Network Discovery [COMPLETED]
PHASE 6  → Nmap Integration [COMPLETED]
PHASE 7  → Automatic Monitoring Scheduler [COMPLETED]
PHASE 8  → Health & Alert Engine [COMPLETED]
PHASE 9  → Web Dashboard (React + TypeScript)
PHASE 10 → Real-Time WebSockets
PHASE 11 → SNMP Hardware Metrics
PHASE 12 → Multi-Channel Notifications
PHASE 13 → Authentication (Spring Security + JWT)
PHASE 14 → Historical Analytics & Reports
PHASE 15 → Performance Optimization
PHASE 16 → Docker & Docker Compose Deployment
```

---

## 📡 REST API Reference

### System Health
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Check backend & PostgreSQL connectivity status |

### Device Management (`/api/devices`)
| Method | Endpoint | Description | Query / Request Parameters |
|---|---|---|---|
| `GET` | `/api/devices` | List all devices | `search` (name/IP/hostname), `type` (Enum), `status` (Enum) |
| `GET` | `/api/devices/{id}` | Get device by ID | — |
| `POST` | `/api/devices` | Register a new device | Request Body (`DeviceRequestDto`) |
| `PUT` | `/api/devices/{id}` | Update existing device | Request Body (`DeviceRequestDto`) |
| `PATCH` | `/api/devices/{id}/toggle-monitoring` | Toggle active monitoring state | — |
| `DELETE` | `/api/devices/{id}` | Delete a device | — |

### Real Ping & Port Monitoring (`/api/devices/{id}`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/devices/{id}/check` | Perform real-time ICMP ping check on target device |
| `GET` | `/api/devices/{id}/metrics` | Fetch historical latency and packet loss metric logs |
| `POST` | `/api/devices/{id}/scan-ports` | Perform TCP port scan on standard service ports |
| `GET` | `/api/devices/{id}/ports` | Fetch latest port status logs for device |
| `POST` | `/api/devices/{id}/nmap-scan` | Perform advanced Nmap scan for single device |

### Network Discovery & Nmap (`/api/discovery`)
| Method | Endpoint | Description | Request Parameters |
|---|---|---|---|
| `POST` | `/api/discovery/scan` | Concurrent subnet CIDR range scan | `subnetCidr` (e.g. `192.168.1.0/24`), `strategy` (`PING`/`TCP`), `threads` |
| `POST` | `/api/discovery/import` | Mass import discovered devices | JSON Array of `DeviceRequestDto` |
| `GET` | `/api/discovery/nmap/status` | Check Nmap binary availability & version | — |
| `POST` | `/api/discovery/nmap` | Run advanced Nmap range scan | Request Body (`DiscoveryRequestDto`) |

### Monitoring Scheduler (`/api/scheduler`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/scheduler/status` | Fetch background worker status, active pool size, and scan metrics |
| `POST` | `/api/scheduler/start` | Enable automatic background monitoring cycle |
| `POST` | `/api/scheduler/stop` | Pause automatic background monitoring cycle |

### Health Alerts (`/api/alerts`)
| Method | Endpoint | Description | Query Parameters |
|---|---|---|---|
| `GET` | `/api/alerts` | List active unresolved alerts | `includeResolved` (boolean) |
| `GET` | `/api/alerts/device/{deviceId}` | List alert history for a device | — |
| `PATCH` | `/api/alerts/{id}/resolve` | Manually acknowledge / resolve an alert | — |

---

## 🏗️ Getting Started

### Prerequisites
- **JDK 21** or higher installed.
- **PostgreSQL** running locally on port `5432` with a database named `myapp` (or update `application.properties`).
- **Nmap** binary installed (`/usr/bin/nmap`).
- **Apache Maven 3.9+**.

### Database Setup
Ensure PostgreSQL is active and accessible:
```bash
psql -U kishore -d myapp -c "SELECT 1;"
```

### Build & Run

#### 1. Compile & Execute Test Suite (53/53 Tests)
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn clean package
```

#### 2. Launch the Spring Boot Server
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

---

## 🧪 Verification & Test Suite

The project includes 53 unit and integration tests using JUnit 5, Mockito, and Spring `WebMvcTest`:
- `DeviceServiceTest` & `DeviceControllerTest`: Device CRUD, validation, and search specs.
- `PingServiceTest` & `DeviceMonitoringServiceTest`: ICMP Ping engine, latency parsing, and metric storage.
- `PortScannerServiceTest` & `MonitoringControllerTest`: TCP socket port scanning and REST APIs.
- `SubnetCalculatorTest`, `DiscoveryServiceTest` & `DiscoveryControllerTest`: Subnet CIDR calculation, concurrent worker pools, and device import.
- `NmapServiceTest`: DOM XML parsing for Nmap scan outputs.
- `HealthAnalyzerServiceTest` & `AlertServiceTest`: Health status rule analysis, state transition alert deduplication, and recovery auto-resolution.
- `MonitoringSchedulerServiceTest` & `SchedulerControllerTest`: Bounded background scheduler pool execution & REST endpoints.
- `AlertControllerTest`: REST API endpoints for viewing and resolving alerts.
- `NetworkMonitorApplicationTests`: Context loading and PostgreSQL Hikari connection pool verification.

---

## 📂 Project Structure

```text
src/
├── main/
│   ├── java/com/networkmonitor/
│   │   ├── NetworkMonitorApplication.java
│   │   ├── config/
│   │   │   └── MonitoringSchedulerConfig.java
│   │   ├── controller/
│   │   │   ├── AlertController.java
│   │   │   ├── DeviceController.java
│   │   │   ├── DiscoveryController.java
│   │   │   ├── HealthCheckController.java
│   │   │   ├── MonitoringController.java
│   │   │   └── SchedulerController.java
│   │   ├── discovery/
│   │   │   ├── DiscoveryStrategy.java
│   │   │   ├── NmapDiscoveryStrategy.java
│   │   │   ├── PingDiscoveryStrategy.java
│   │   │   ├── SubnetCalculator.java
│   │   │   └── TcpDiscoveryStrategy.java
│   │   ├── dto/
│   │   │   ├── AlertResponseDto.java
│   │   │   ├── DeviceRequestDto.java
│   │   │   ├── DeviceResponseDto.java
│   │   │   ├── DiscoveredDeviceDto.java
│   │   │   ├── DiscoveryRequestDto.java
│   │   │   ├── DiscoveryResponseDto.java
│   │   │   ├── MetricResponseDto.java
│   │   │   ├── NmapHostResultDto.java
│   │   │   ├── NmapPortResultDto.java
│   │   │   ├── NmapScanResultDto.java
│   │   │   ├── PingCheckResponseDto.java
│   │   │   ├── PortScanResponseDto.java
│   │   │   └── PortStatusDto.java
│   │   ├── entity/
│   │   │   ├── Alert.java
│   │   │   ├── AlertSeverity.java
│   │   │   ├── AlertType.java
│   │   │   ├── Device.java
│   │   │   ├── DeviceStatus.java
│   │   │   ├── DeviceType.java
│   │   │   ├── HealthStatus.java
│   │   │   ├── MonitoringMetric.java
│   │   │   ├── PortState.java
│   │   │   └── PortStatus.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── monitoring/
│   │   │   ├── NmapService.java
│   │   │   ├── PingResult.java
│   │   │   ├── PingService.java
│   │   │   └── PortScannerService.java
│   │   ├── repository/
│   │   │   ├── AlertRepository.java
│   │   │   ├── DeviceRepository.java
│   │   │   ├── MonitoringMetricRepository.java
│   │   │   └── PortStatusRepository.java
│   │   └── service/
│   │       ├── AlertService.java
│   │       ├── DeviceMonitoringService.java
│   │       ├── DeviceService.java
│   │       ├── DiscoveryService.java
│   │       ├── HealthAnalyzerService.java
│   │       └── MonitoringSchedulerService.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/networkmonitor/
        ├── NetworkMonitorApplicationTests.java
        ├── controller/
        │   ├── AlertControllerTest.java
        │   ├── DeviceControllerTest.java
        │   ├── DiscoveryControllerTest.java
        │   ├── MonitoringControllerTest.java
        │   └── SchedulerControllerTest.java
        ├── discovery/
        │   └── SubnetCalculatorTest.java
        ├── monitoring/
        │   ├── NmapServiceTest.java
        │   ├── PingServiceTest.java
        │   └── PortScannerServiceTest.java
        └── service/
            ├── AlertServiceTest.java
            ├── DeviceMonitoringServiceTest.java
            ├── DeviceServiceTest.java
            ├── DiscoveryServiceTest.java
            ├── HealthAnalyzerServiceTest.java
            └── MonitoringSchedulerServiceTest.java
```
