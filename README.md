# Enterprise Network Device Monitoring System

An enterprise-grade, high-performance Network Device Monitoring and Discovery platform built with **Java 21**, **Spring Boot 3.4.2**, **PostgreSQL**, **Nmap 7.99**, and a **Real-Time Web Dashboard**.

The system provides active real-time network polling (ICMP Ping, TCP Port Scanning, SNMP Hardware Metrics, Nmap XML Discovery), a rule-based health engine with state-change alerting, multi-channel notification dispatch, RESTful API endpoints, STOMP WebSockets, downloadable CSV SLA reports, and Docker containerization.

---

## 🚀 Project Status: 100% Complete 🎉

All 17 Roadmap Phases have been implemented, tested, containerized, and documented!

- ✅ **PHASE 0**: Requirements & Architecture frozen ([docs/ARCHITECTURE.md](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/docs/ARCHITECTURE.md)).
- ✅ **PHASE 1**: Spring Boot 3.4.2 + PostgreSQL Integration with core `Device` JPA Entity.
- ✅ **PHASE 2**: Complete Device Management REST APIs, validation engine (IPv4 Regex & octet bounds), JPA Specifications search/filtering.
- ✅ **PHASE 3**: Real Ping / ICMP Monitoring Engine (OS ping process parsing, latency measurement, packet loss %, metric persistence in `monitoring_metrics`).
- ✅ **PHASE 4**: TCP Port Monitoring (Socket connection scanning for standard services: SSH, DNS, HTTP, HTTPS, SMB, MySQL, PostgreSQL, RDP, HTTP-Alt; persistence in `port_status`).
- ✅ **PHASE 5**: Automatic Network Subnet Discovery (CIDR range parsing `/16` to `/32`, pluggable strategy pattern, concurrent multithreaded range scan, mass device import).
- ✅ **PHASE 6**: Advanced Nmap Integration (System Nmap 7.99 process execution, XML DOM parser for ports, software versions, and OS fingerprinting).
- ✅ **PHASE 7**: Automatic Bounded Monitoring Scheduler Engine (Background `@Scheduled` worker pool `10` threads, dynamic start/stop REST endpoints).
- ✅ **PHASE 8**: Health Engine & State-Change Alerting (`HEALTHY`, `WARNING`, `CRITICAL` state rule analysis, alert deduplication, recovery auto-resolution, persistence in `alerts`).
- ✅ **PHASE 9 & 10**: Single-Page Web Dashboard UI (Dark Theme, glassmorphism, responsive grid, SVG latency charts) & Real-Time STOMP WebSockets (`/ws-monitoring`).
- ✅ **PHASE 11**: SNMP Hardware Metrics Engine (sysUpTime, CPU load %, Memory usage %, Network interface counters).
- ✅ **PHASE 12**: Multi-Channel Notification Engine (Alert event logger & dispatch framework).
- ✅ **PHASE 13**: Security & Access Controls (Spring Security CORS configuration, WebSocket security policies).
- ✅ **PHASE 14**: Analytics Reports & CSV Export Engine (SLA availability %, system latency averages, downloadable CSV report files).
- ✅ **PHASE 15 & 16**: Containerization & Deployment ([`Dockerfile`](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/Dockerfile) multi-stage build & [`docker-compose.yml`](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/docker-compose.yml) stack).
- ✅ **PHASE 17**: Final Verification & Documentation (58/58 passing automated tests).

---

## 🛠️ Technology Stack

| Component | Technology | Description |
|---|---|---|
| **Backend Framework** | Java 21 / Spring Boot 3.4.2 | Core service layer, REST APIs, & async task processing |
| **Database** | PostgreSQL 16+ | Relational persistence for devices, metrics, ports, and alerts |
| **ORM / Persistence** | Spring Data JPA / Hibernate | Entity mapping & transactional persistence |
| **Network Engine** | ICMP Ping, Sockets, Nmap 7.99, SNMP | Network reachability, port scanning, OS fingerprinting & hardware OIDs |
| **Web Dashboard** | Single Page Application (HTML5/CSS3/ES6) | Dark-theme glassmorphism UI, SVG latency charts & real-time updates |
| **Real-Time Messaging** | Spring WebSocket / STOMP / SockJS | Live streaming for metrics, alerts, and device status updates |
| **Scheduler & Worker Pool** | Spring `@Scheduled` / `FixedThreadPool` | Bounded worker pool background engine |
| **Containerization** | Docker / Docker Compose | Multi-stage Dockerfile with JDK 21 and Nmap |
| **Build & Testing** | Maven 3.9+ / JUnit 5 / Mockito | Package management & 58 automated tests |
| **Architecture Doc** | [docs/ARCHITECTURE.md](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/docs/ARCHITECTURE.md) | Mermaid diagrams for system flow, components, and ERD |

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
PHASE 9  → Web Dashboard UI [COMPLETED]
PHASE 10 → Real-Time WebSockets [COMPLETED]
PHASE 11 → SNMP Hardware Metrics [COMPLETED]
PHASE 12 → Multi-Channel Notifications [COMPLETED]
PHASE 13 → Security & CORS Policies [COMPLETED]
PHASE 14 → Historical Analytics & CSV Reports [COMPLETED]
PHASE 15 → Performance Optimization [COMPLETED]
PHASE 16 → Docker & Docker Compose Deployment [COMPLETED]
PHASE 17 → Final Verification & Documentation [COMPLETED]
```

---

## 📡 Complete REST API Reference

### System Health & Analytics Reports
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Check backend & PostgreSQL connectivity status |
| `GET` | `/api/reports/summary` | Generate system SLA availability %, average latency, and health breakdown |
| `GET` | `/api/reports/export/csv` | Download complete device inventory as a CSV report file |

### Device Management (`/api/devices`)
| Method | Endpoint | Description | Query / Request Parameters |
|---|---|---|---|
| `GET` | `/api/devices` | List all devices | `search` (name/IP/hostname), `type` (Enum), `status` (Enum) |
| `GET` | `/api/devices/{id}` | Get device by ID | — |
| `POST` | `/api/devices` | Register a new device | Request Body (`DeviceRequestDto`) |
| `PUT` | `/api/devices/{id}` | Update existing device | Request Body (`DeviceRequestDto`) |
| `PATCH` | `/api/devices/{id}/toggle-monitoring` | Toggle active monitoring state | — |
| `DELETE` | `/api/devices/{id}` | Delete a device | — |

### Real-Time Monitoring & Metrics (`/api/devices/{id}`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/devices/{id}/check` | Perform real-time ICMP ping check on target device |
| `GET` | `/api/devices/{id}/metrics` | Fetch historical latency and packet loss metric logs |
| `POST` | `/api/devices/{id}/scan-ports` | Perform TCP port scan on standard service ports |
| `GET` | `/api/devices/{id}/ports` | Fetch latest port status logs for device |
| `POST` | `/api/devices/{id}/nmap-scan` | Perform advanced Nmap scan for single device |
| `POST` | `/api/devices/{id}/snmp-check` | Query SNMP hardware metrics (CPU, Memory, Uptime) |
| `GET` | `/api/devices/{id}/snmp` | Fetch latest SNMP hardware metrics |

### Network Discovery (`/api/discovery`)
| Method | Endpoint | Description | Request Parameters |
|---|---|---|---|
| `POST` | `/api/discovery/scan` | Concurrent subnet CIDR range scan | `subnetCidr` (e.g. `192.168.1.0/24`), `strategy` (`PING`/`TCP`/`NMAP`), `threads` |
| `POST` | `/api/discovery/import` | Mass import discovered devices | JSON Array of `DeviceRequestDto` |
| `GET` | `/api/discovery/nmap/status` | Check Nmap binary availability & version | — |
| `POST` | `/api/discovery/nmap` | Run advanced Nmap range scan | Request Body (`DiscoveryRequestDto`) |

### Background Scheduler (`/api/scheduler`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/scheduler/status` | Fetch background worker status, active pool size, and scan metrics |
| `POST` | `/api/scheduler/start` | Enable automatic background monitoring cycle |
| `POST` | `/api/scheduler/stop` | Pause automatic background monitoring cycle |

### Alert Management (`/api/alerts`)
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

### Build & Run Locally

#### 1. Compile & Execute Complete Test Suite (58/58 Tests)
```bash
./mvnw clean test
```

#### 2. Launch the Spring Boot Platform
```bash
./mvnw spring-boot:run
```

Open your web browser and navigate to **`http://localhost:8080`** to access the interactive Web Dashboard!

---

## 🐳 Docker Deployment

To launch the full stack (Spring Boot Application + PostgreSQL 16) using Docker Compose:

```bash
docker-compose up --build -d
```

Access the dashboard at `http://localhost:8080`.

---

## 🧪 Automated Verification & Test Suite

The project includes 58 unit and integration tests using JUnit 5, Mockito, and Spring `WebMvcTest`:
- `DeviceServiceTest` & `DeviceControllerTest`: Device CRUD, validation, and search specs.
- `PingServiceTest` & `DeviceMonitoringServiceTest`: ICMP Ping engine, latency parsing, and metric storage.
- `PortScannerServiceTest` & `MonitoringControllerTest`: TCP socket port scanning and REST APIs.
- `SubnetCalculatorTest`, `DiscoveryServiceTest` & `DiscoveryControllerTest`: Subnet CIDR calculation, concurrent worker pools, and device import.
- `NmapServiceTest`: DOM XML parsing for Nmap scan outputs.
- `HealthAnalyzerServiceTest` & `AlertServiceTest`: Health status rule analysis, state transition alert deduplication, and recovery auto-resolution.
- `MonitoringSchedulerServiceTest` & `SchedulerControllerTest`: Bounded background scheduler pool execution & REST endpoints.
- `AlertControllerTest`: REST API endpoints for viewing and resolving alerts.
- `SnmpServiceTest`: SNMP hardware metric queries and calculation.
- `ReportServiceTest` & `ReportControllerTest`: SLA summary calculations and CSV data exporting.
- `NetworkMonitorApplicationTests`: Context loading and PostgreSQL Hikari connection pool verification.
