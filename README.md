# DeliveryWise

![Java](https://img.shields.io/badge/Java-21_Corretto-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen?logo=springboot)
![Hibernate](https://img.shields.io/badge/Hibernate-6.x-59666C?logo=hibernate)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![PostGIS](https://img.shields.io/badge/PostGIS-3.6-green)
![pgRouting](https://img.shields.io/badge/pgRouting-4.0.1-blue)
![OR-Tools](https://img.shields.io/badge/Google%20OR--Tools-9.8-4285F4?logo=google)
![GraphHopper](https://img.shields.io/badge/GraphHopper-latest-75b800)
![JSoup](https://img.shields.io/badge/JSoup-HTML%20Parser-yellow)
![Lombok](https://img.shields.io/badge/Lombok-latest-red)
![MapStruct](https://img.shields.io/badge/MapStruct-latest-blueviolet)
![Flyway](https://img.shields.io/badge/Flyway-latest-CC0200)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

**Delivery route optimization system for enterprises.**

Система оптимізації маршрутів доставки для підприємств.

---

## What it does / Що робить

- Parses client data (addresses, names, weight, amount, notes) from HTML tables — *JSoup*
- Maps parsed data (DTO) to database entities — *MapStruct*
- Stores geodata and builds route graphs — *PostgreSQL + PostGIS + pgRouting*
- Optimizes delivery distribution across vehicles — *Google OR-Tools*
- Calculates real road routes using OpenStreetMap data — *GraphHopper (local Docker server)*
- Manages database schema versions — *Flyway*

---

## Tech Stack / Стек технологій

| Component | Version | Purpose |
|---|---|---|
| Java (Amazon Corretto) | 21 | Runtime — мова програмування |
| Spring Boot | 3.3.5 | Backend framework — каркас додатку |
| Hibernate | 6.x | ORM — робота з БД через Java-об'єкти |
| Lombok | latest | Прибирає шаблонний код (геттери, сеттери) |
| MapStruct | latest | Маппінг DTO → Entity (парсер → БД) |
| Flyway | latest | Версіонування схеми БД (міграції) |
| PostgreSQL | 16 | Database — реляційна база даних |
| PostGIS | 3.6 | Геопросторове розширення PostgreSQL |
| pgRouting | 4.0.1 | Мережеві алгоритми всередині БД |
| Google OR-Tools | 9.8 | Оптимізація маршрутів (VRP задача) |
| GraphHopper | latest | Реальні дороги по OSM-карті (Docker) |
| JSoup | latest | Парсинг HTML-таблиць з даними клієнтів |
| HikariCP | 6.0 | Connection pool — пул з'єднань з БД |
| Testcontainers | 1.19 | Інтеграційні тести з реальною БД у Docker |

### Routing strategy / Стратегія маршрутизації

- **GraphHopper** — головний по дорогах. Рахує реальні відстані та час по картах України (OSM).
- **pgRouting** — для мережевого аналізу всередині БД (якщо знадобиться складна графова логіка).

---

## Environment Setup / Встановлення оточення

### 1. Java 21 (Amazon Corretto)

Download from [corretto.aws](https://corretto.aws/downloads/latest/amazon-corretto-21-x64-windows-jdk.msi)

### 2. PostgreSQL 16

Download from [enterprisedb.com](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)

Default install path: `C:\Program Files\PostgreSQL\16\`

### 3. PostGIS + pgRouting

PostGIS Bundle 3.6 already includes pgRouting 4.0.1 — no separate download needed.

Run **Stack Builder** (installed with PostgreSQL):

- Category **Spatial Extensions** → **PostGIS 3.6 Bundle for PostgreSQL 16 (64 bit)**
- Check during install:
    - ✅ PostGIS Bundle
    - ✅ Register PROJ_LIB Env Var
    - ✅ Register GDAL_DATA Env Var
    - ✅ Enable Key GDAL Drivers
    - ✅ Enable All GDAL Drivers
    - ✅ Allow Out-db Rasters
    - ✅ Register SSL Bundle
    - ⬜ Create spatial database *(not needed)*

### 4. Enable extensions in your database

Run in pgAdmin or psql:

```sql
CREATE EXTENSION postgis;
CREATE EXTENSION pgrouting CASCADE;
```

Verify:

```sql
SELECT PostGIS_Version();
-- Expected: 3.6 USE_GEOS=1 USE_PROJ=1 USE_STATS=1

SELECT * FROM pgr_version();
-- Expected: 4.0.1
```

### 5. GraphHopper

*(to be added — Docker setup with Ukraine OSM map)*

### 6. Google OR-Tools

Already included via Maven dependency — no separate install needed.

---

## Running the Project / Запуск

*(to be added)*

---

## Database Structure / Структура бази даних

*(to be added)*

---

## Testing / Тестування

The project uses **Testcontainers** — integration tests spin up a real PostgreSQL instance in Docker automatically. No manual DB setup needed for tests.

```bash
mvn test
```

---

## Roadmap (v1.0)

| Stage | Task | Tools | Expected Result / Очікуваний результат |
|---|---|---|---|
| **0. Core** | DB design & migrations | Flyway, PostGIS | Schema versioned in `/db/migration`. `mvn flyway:migrate` runs clean. Tables exist with PostGIS geometry columns |
| **1. Ingestion** | Parse raw HTML client data | JSoup | Given an HTML file with a client table → Java object list with name, address, weight, amount, notes populated correctly |
| **2. Mapping** | Transform DTO → Entity | MapStruct, Lombok | DTO from parser maps to Entity without manual field assignment. Unit test green |
| **3. Geo** | Geocoding & road graphs | GraphHopper (Docker), OSM Ukraine | Given an address string → returns lat/lon. Given two points → returns distance in km and travel time |
| **4. Engine** | Route optimization math (VRP) | Google OR-Tools | Given N clients and K vehicles → returns optimal delivery distribution. Total distance minimized vs naive sequential order |
| **5. Interface** | REST API + GeoJSON output | Spring REST | `POST /api/routes` accepts client list → returns GeoJSON with optimized routes. Testable via Postman |
| **6. Deploy** | System orchestration + UI | Docker Compose, Leaflet.js | `docker compose up` starts the full system. Routes visible on interactive map in browser |

---

## Expected Final Result / Фінальний очікуваний результат

> Менеджер завантажує HTML-файл з таблицею клієнтів.
> Система повертає оптимізовані маршрути для кожного водія —
> з реальними дорогами, відстанями та порядком доставок.
> Маршрути відображаються на карті та доступні через REST API.

**In numbers / В цифрах:**
- Input: HTML table with clients (address, weight, amount, notes)
- Output: N optimized routes for K vehicles
- Optimization target: minimize total distance driven
- Map: real roads of Ukraine (OpenStreetMap)
- Deployment: single `docker compose up` on enterprise server

> 🗺️ Leaflet.js (interactive map UI) is planned for stage 6 — together with a frontend teammate or as a minimal single-page add-on.
