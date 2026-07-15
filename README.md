# DeliveryWise

Delivery route optimization system for enterprises.  
Система оптимізації маршрутів доставки для підприємств.

---

## What it does / Що робить

* **Parses client data** (addresses, names, weight, amount, notes) from unstructured corporate formats — `JSoup`
* **Maps parsed data (DTO)** directly to robust database business models — `MapStruct`
* **Stores geospatial data** and handles route graphs — `PostgreSQL` + `PostGIS` + `pgRouting`
* **Optimizes delivery distribution** across active corporate vehicles under constraints (Capacity, Time Windows) — `Google OR-Tools`
* **Calculates real road routing matrices** using OpenStreetMap routing APIs — `OSRM` / `GraphHopper`
* **Manages database schema evolution** automatically — `Flyway`

---

## Tech Stack / Стек технологій

| Component | Version | Purpose / Призначення |
| :--- | :--- | :--- |
| **Java (Amazon Corretto)** | `21` | Runtime engine — сучасна мова програмування |
| **Spring Boot** | `3.3.5` | Backend core framework — каркас додатку |
| **Hibernate** | `6.x` | ORM layer — безпечна робота з БД через Java-об'єкти |
| **Lombok** | `1.18.x` | Boilerplate reduction — прибирає шаблонний код |
| **MapStruct** | `1.5.x` | DTO → Entity lightning fast compiling mapper |
| **Flyway** | `10.x` | Database schema version control — міграції структури |
| **PostgreSQL** | `16` | Core relational enterprise database |
| **PostGIS** | `3.6` | Geospatial extension for advanced spatial queries |
| **pgRouting** | `4.0.1` | Network analysis routing algorithms inside DB |
| **Google OR-Tools** | `9.8` | Advanced Vehicle Routing Problem (VRP) optimization math |
| **OSRM** | *Active* | Real road distance matrix calculation over OpenStreetMap data |
| **JSoup** | `1.18.x` | Secure HTML table data ingestion and parsing |
| **HikariCP** | `6.0` | High-performance database connection pooling |
| **Testcontainers** | `1.19.x` | Isolated integration testing with real Docker DB instances |

---

## Deferred Features
- **operationType auto-detection**: parse `specialNotes` column using keyword matching
  (повернення/забрати/забір → RETURN, перевантаження → TRANSFER).
  One notes field may contain multiple hints. Implement after JSoup parser is stable.
- **Logistics settings panel**: configurable `serviceTimeMinutes` per point,
  traffic density coefficients (e.g. Friday peak hours).
  Includes "вивантаження вручну" flag → extended service time.

---

## Future Architecture (SaaS)
- Input abstraction layer: support multiple input formats
  (raw address / single coordinate pair / two coordinate pairs: load + delivery point)
- Format auto-detection before geocoding pipeline
- Anonymization layer for coordinate pairs

---

## Routing Strategy & Infrastructure / Стратегія маршрутизації та інфраструктура

*   **OSRM (Open Source Routing Machine)** — Основний високопродуктивний движок для обчислення геопространствених матриць. Розраховує реальні дорожні відстані та часові проміжки по картах України на основі даних OpenStreetMap (OSM), повністю замінюючи примітивну геометричну дистанцію (Хаверсин). Ізольований на рівні інтерфейсу `DistanceProvider`.
*   **PostGIS & pgRouting** — Використовуються для збереження географічних координат, геозонування та додаткового просторового аналізу безпосередньо всередині СУБД PostgreSQL. Забезпечують інфраструктуру для накладання «Віртуальних стін».

### Planned Infrastructure Upgrades (TODO Roadmap)
1. **Smart Statistics (Розумна статистика):** Впровадження динамічних OSRM-профілей швидкості залежно від дня тижня та часу виїзду (наприклад, п'ятничний трафік на виїзд з Києва через проспект Перемоги/Глушкова).
2. **Virtual Walls (Віртуальні стіни):** Перевірка перетину маршрутних векторів із полігонами заблокованих логістами зон (`blocked_zones`) у PostGIS із штучним завищенням ваг для Google OR-Tools (вимушений об'їзд проблемних ділянок).

---

## Environment Setup / Встановлення оточення

### 1. Database Infrastructure
* Install **PostgreSQL 16**.
* Use Stack Builder to install the **PostGIS 3.6 Bundle** (which natively includes **pgRouting 4.0.1**).
* Initialize spatial extensions inside your target database (`deliverywise`):

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgrouting CASCADE;

### 2. Properties Configuration
Ensure your local sensitive configuration parameters are securely separated. Create a `src/main/resources/application-local.properties` file (excluded from Git) to override your real database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/deliverywise
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
carrier.client.mode=stub

Running the Project / Запуск проекту
To launch the system locally with active secure profile constraints:

mvn clean spring-boot:run -Dspring-boot.run.profiles=local

Testing / Тестування
The engine uses Testcontainers. Integration tests spin up an isolated PostgreSQL instance inside Docker automatically, bypassing the need for manual test-database setup:

mvn test

Architecture Roadmap (v1.0)

## Architecture Roadmap (v1.0)

| Stage | Task | Tools | Expected Result / Очікуваний результат | Status |
| :---: | :--- | :--- | :--- | :---: |
| **0** | **Core DB Setup** | Flyway, PostGIS | Schema versioned in `/db/migration`. Clean execution without checksum conflicts. | ✅ |
| **1** | **Ingestion Layer** | JSoup | Unstructured HTML input parsed cleanly into structured Java DTO models. | ✅ |
| **2** | **Data Mapping** | MapStruct | Lightning-fast compiled mapping from DTO layer into database Entities. | ✅ |
| **3** | **Geo & Routing** | OSRM, PostGIS | Given address strings → coordinates generated. Matrix calculation via real OSM road tracks. | ✅ |
| **4** | **VRP Engine** | Google OR-Tools | Advanced Vehicle Routing Math execution. Total distance minimized under capacity constraints. | 🛠️ *In Progress* |
| **5** | **REST Interface** | Spring REST | HTTP endpoints accepting data payloads and returning optimized GeoJSON route streams. | ⏳ *Planned* |
| **6** | **Deployment** | Docker Compose | Complete system orchestration via single command with interactive Leaflet.js map UI. | ⏳ *Planned* |

Expected Final Result / Фінальний очікуваний результат

Менеджер завантажує HTML-файл з таблицею клієнтів. Система повертає оптимізовані маршрути для кожного водія — з реальними дорогами, відстанями та порядком доставок. Маршрути відображаються на інтерактивній карті та доступні через REST API.

Target: Minimize total enterprise logistics distance driven.

Map: Real operational routing network of Ukraine (OSM data).

Deployment: Production-ready single docker compose up topology.
