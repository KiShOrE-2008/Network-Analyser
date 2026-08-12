# Enterprise Network Device Monitoring System

An enterprise-grade, high-performance Network Device Monitoring and Discovery backend built with **Java 21**, **Spring Boot 3.4.2**, and **PostgreSQL**.

The system provides real-time active network polling (ICMP Ping, TCP Port Scanning, SNMP metric extraction, Nmap discovery), an intelligent health rule engine with alerting, a RESTful API backend, and WebSocket integration for dynamic web dashboards.

---

## 🚀 Current Project Status

- ✅ **PHASE 0**: Requirements & Architecture frozen ([ARCHITECTURE.md](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/ARCHITECTURE.md)).
- ✅ **PHASE 1**: Spring Boot 3.4.2 + PostgreSQL Integration with core `Device` JPA Entity.
- ✅ **PHASE 2**: Complete Device Management REST APIs, validation engine (IPv4 Regex & octet bounds), JPA Specifications search/filtering, and unit/integration test suite.
- ⏳ **PHASE 3** *(Next)*: Real Ping / ICMP Monitoring Service.

---

## 🛠️ Technology Stack

| Component | Technology | Description |
|---|---|---|
| **Backend Framework** | Java 21 / Spring Boot 3.4.2 | Core service layer, REST APIs, & async task processing |
| **Database** | PostgreSQL 16+ | Relational persistence for devices, metrics, and alerts |
| **ORM / Persistence** | Spring Data JPA / Hibernate | Entity mapping & transactional persistence |
| **Input Validation** | Jakarta Validation | DTO constraints, IPv4 regex, & custom interval checks |
| **Build & Testing** | Maven 3.9+ / JUnit 5 / Mockito | Package management & automated testing |
| **Architecture Doc** | [ARCHITECTURE.md](file:///run/media/kishore/Data/Project/3/java/NetworkDeviceMonitoringDemo/ARCHITECTURE.md) | Mermaid diagrams for system flow, components, and ERD |

---

## 🗺️ Project Roadmap & Milestones

```text
PHASE 0  → Requirements + Architecture [COMPLETED]
PHASE 1  → Java/Spring Boot + PostgreSQL [COMPLETED]
PHASE 2  → Device Management APIs [COMPLETED]
PHASE 3  → Real Ping/ICMP Monitoring
PHASE 4  → TCP Port Monitoring
PHASE 5  → Subnet Network Discovery
PHASE 6  → Nmap Integration
PHASE 7  → Automatic Monitoring Scheduler
PHASE 8  → Health & Alert Engine
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

#### Example Request Body (`POST /api/devices`)
```json
{
  "name": "Core Gateway Router",
  "ipAddress": "192.168.1.1",
  "hostname": "gateway.internal",
  "deviceType": "ROUTER",
  "vendor": "Cisco",
  "model": "ISR 4331",
  "monitoringEnabled": true,
  "scanInterval": 10
}
```

---

## 🏗️ Getting Started

### Prerequisites
- **JDK 21** or higher installed.
- **PostgreSQL** running locally on port `5432` with a database named `myapp` (or update `application.properties`).
- **Apache Maven 3.9+**.

### Database Setup
Ensure PostgreSQL is active and accessible:
```bash
psql -U kishore -d myapp -c "SELECT 1;"
```

### Build & Run

#### 1. Compile & Execute Unit Tests
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn clean package
```

#### 2. Launch the Spring Boot Server
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

#### 3. Test Health Endpoint
```bash
curl http://localhost:8080/api/health
```

---

## 🧪 Verification & Test Suite

The project includes unit and integration tests using JUnit 5, Mockito, and Spring `WebMvcTest`:
- `DeviceServiceTest`: Validates CRUD logic, duplicate IP rejection, IP octet bounds, and search specifications.
- `DeviceControllerTest`: Validates REST endpoints, JSON serialization, and HTTP status codes (`200`, `201`, `204`, `400`, `404`).
- `NetworkMonitorApplicationTests`: Verifies Spring context loading and PostgreSQL connection pool.

---

## 📂 Project Structure

```text
src/
├── main/
│   ├── java/com/networkmonitor/
│   │   ├── NetworkMonitorApplication.java
│   │   ├── controller/
│   │   │   ├── DeviceController.java
│   │   │   └── HealthCheckController.java
│   │   ├── dto/
│   │   │   ├── DeviceRequestDto.java
│   │   │   └── DeviceResponseDto.java
│   │   ├── entity/
│   │   │   ├── Device.java
│   │   │   ├── DeviceStatus.java
│   │   │   ├── DeviceType.java
│   │   │   └── HealthStatus.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── repository/
│   │   │   └── DeviceRepository.java
│   │   └── service/
│   │       └── DeviceService.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/networkmonitor/
        ├── NetworkMonitorApplicationTests.java
        ├── controller/DeviceControllerTest.java
        └── service/DeviceServiceTest.java
```
