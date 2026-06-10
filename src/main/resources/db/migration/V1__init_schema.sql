-- noinspection ALL

-- ============================================================
-- DeliveryWise — V1__init_schema.sql
-- Core Logistics Infrastructure Schema Initialization
-- ============================================================

CREATE EXTENSION IF NOT EXISTS postgis;

-- ------------------------------------------------------------
-- 1. depots
-- ------------------------------------------------------------
CREATE TABLE depots (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    address     VARCHAR(255)    NOT NULL,
    location    geometry(Point, 4326) NOT NULL
);

-- Sanitized center point (Enterprise Central Hub placeholder)
INSERT INTO depots (name, address, location)
VALUES (
    'Enterprise Central Hub — Kyiv',
    'Main Logistics St, 1, Kyiv, Ukraine',
    ST_SetSRID(ST_MakePoint(30.5234, 50.4501), 4326)
);

-- ------------------------------------------------------------
-- 2. vehicles
-- ------------------------------------------------------------
-- Synchronized with io.deliverywise.core.route.model.Vehicle.FleetType
CREATE TYPE vehicle_fleet_type AS ENUM ('OWN', 'RE_HIRED', 'NOVA_POSHTA');

CREATE TABLE vehicles (
    id                  BIGSERIAL           PRIMARY KEY,
    depot_id            BIGINT              NOT NULL REFERENCES depots(id),
    name                VARCHAR(100)        NOT NULL,
    license_plate       VARCHAR(20),
    capacity_kg         NUMERIC(8,2)        NOT NULL,
    capacity_pallets    SMALLINT            NOT NULL,
    fleet_type          vehicle_fleet_type  NOT NULL DEFAULT 'OWN'
);

INSERT INTO vehicles (depot_id, name, license_plate, capacity_kg, capacity_pallets, fleet_type)
VALUES
    (1, 'Corporate Van Light 01',  NULL, 1865.00, 4, 'OWN'),
    (1, 'Corporate Van Light 02',  NULL, 1865.00, 4, 'OWN'),
    (1, 'Standard Truck Heavy 01', NULL, 4500.00, 9, 'OWN');

-- ------------------------------------------------------------
-- 3. clients
-- ------------------------------------------------------------
CREATE TABLE clients (
    id                  BIGSERIAL       PRIMARY KEY,
    name                VARCHAR(200)    NOT NULL,
    address             VARCHAR(255)    NOT NULL,
    location            geometry(Point, 4326),      -- Populated via Geocoding Service (Stage 3)
    delivery_zone       VARCHAR(50),                -- Anonymized operational sectors: Zone_A, Zone_B
    min_order_amount    NUMERIC(12,2)   NOT NULL DEFAULT 5000.00
);

-- ------------------------------------------------------------
-- 4. delivery_orders
-- ------------------------------------------------------------
CREATE TABLE delivery_orders (
    id              BIGSERIAL       PRIMARY KEY,
    client_id       BIGINT          NOT NULL REFERENCES clients(id),
    weight_kg       NUMERIC(8,2)    NOT NULL CHECK (weight_kg > 0),
    pallets         SMALLINT        NOT NULL DEFAULT 0 CHECK (pallets >= 0),
    order_amount    NUMERIC(12,2)   NOT NULL CHECK (order_amount >= 0),
    delivery_date   DATE            NOT NULL
);

-- ------------------------------------------------------------
-- 5. routes
-- ------------------------------------------------------------
CREATE TABLE routes (
    id                  BIGSERIAL       PRIMARY KEY,
    depot_id            BIGINT          NOT NULL REFERENCES depots(id),
    vehicle_id          BIGINT          NOT NULL REFERENCES vehicles(id),
    route_date          DATE            NOT NULL,
    total_distance_m    INTEGER         -- Populated after Google OR-Tools optimization execution
);

-- ------------------------------------------------------------
-- 6. route_stops
-- ------------------------------------------------------------
CREATE TABLE route_stops (
    id              BIGSERIAL       PRIMARY KEY,
    route_id        BIGINT          NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    order_id        BIGINT          NOT NULL REFERENCES delivery_orders(id),
    stop_sequence   SMALLINT        NOT NULL CHECK (stop_sequence >= 0),
    service_time    INTERVAL        NOT NULL DEFAULT '15 minutes',

    UNIQUE (route_id, stop_sequence),
    UNIQUE (route_id, order_id)
);

-- ------------------------------------------------------------
-- Data Access Optimization (Indexes)
-- ------------------------------------------------------------
CREATE INDEX idx_clients_location        ON clients       USING GIST (location);
CREATE INDEX idx_depots_location         ON depots        USING GIST (location);
CREATE INDEX idx_delivery_orders_date    ON delivery_orders (delivery_date);
CREATE INDEX idx_delivery_orders_client  ON delivery_orders (client_id);
CREATE INDEX idx_routes_date             ON routes (route_date);
CREATE INDEX idx_route_stops_route       ON route_stops (route_id);