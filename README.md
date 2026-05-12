# DeliveryWise

![Java](https://img.shields.io/badge/Java-17+-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)
![Hibernate](https://img.shields.io/badge/Hibernate-6.x-59666C?logo=hibernate)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![PostGIS](https://img.shields.io/badge/PostGIS-3.6-green)
![pgRouting](https://img.shields.io/badge/pgRouting-4.0.1-blue)
![OR-Tools](https://img.shields.io/badge/Google%20OR--Tools-latest-4285F4?logo=google)
![GraphHopper](https://img.shields.io/badge/GraphHopper-latest-75b800)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

Система оптимізації маршрутів доставки.

## Стек технологій

| Компонент | Версія |
|---|---|
| Java / Spring Boot | 17+ / 3.x |
| Hibernate | 6.x |
| PostgreSQL | 16 |
| PostGIS | 3.6 |
| pgRouting | 4.0.1 |
| Google OR-Tools | — |
| GraphHopper | — |

---

## Встановлення оточення

### 1. PostgreSQL 16

Завантажити з [enterprisedb.com](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)

Стандартний шлях встановлення: `C:\Program Files\PostgreSQL\16\`

---

### 2. PostGIS + pgRouting

PostGIS Bundle 3.6 вже включає pgRouting 4.0.1 — окремо качати не потрібно.

Запустити **Stack Builder** (встановлюється разом з PostgreSQL):

- Категорія **Spatial Extensions** → **PostGIS 3.6 Bundle for PostgreSQL 16 (64 bit)**
- Під час встановлення відмітити:
    - ✅ PostGIS Bundle
    - ✅ Register PROJ_LIB Env Var
    - ✅ Register GDAL_DATA Env Var
    - ✅ Enable Key GDAL Drivers
    - ✅ Enable All GDAL Drivers
    - ✅ Allow Out-db Rasters
    - ✅ Register SSL Bundle
    - ⬜ Create spatial database *(не потрібно)*

---

### 3. Підключення розширень до бази даних

Після створення бази даних виконати в pgAdmin або psql:

```sql
CREATE EXTENSION postgis;
CREATE EXTENSION pgrouting CASCADE;
```

Перевірка:

```sql
SELECT PostGIS_Version();
SELECT * FROM pgr_version();
```

Очікуваний результат:
- PostGIS: `3.6 USE_GEOS=1 USE_PROJ=1 USE_STATS=1`
- pgRouting: `4.0.1`

---

### 4. Google OR-Tools

*( буде доповнено )*

---

### 5. GraphHopper

*( буде доповнено )*

---

## Запуск проекту

*( буде доповнено )*

---

## Структура бази даних

*( буде доповнено )*
