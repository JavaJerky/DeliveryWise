-- ============================================================
-- DeliveryWise — V2__add_operational_fields.sql
-- Stage 1: Operational Fields & Logistics Analytics Constraints
-- ============================================================

-- ------------------------------------------------------------
-- delivery_orders: detailed cargo and warehouse distribution
-- ------------------------------------------------------------
ALTER TABLE delivery_orders
    ADD COLUMN external_id                 BIGINT          UNIQUE,
    ADD COLUMN cargo_spaces_raw            VARCHAR(100),
    ADD COLUMN main_warehouse_places       SMALLINT        NOT NULL DEFAULT 0 CHECK (main_warehouse_places >= 0),
    ADD COLUMN fragile_warehouse_places    SMALLINT        NOT NULL DEFAULT 0 CHECK (fragile_warehouse_places >= 0),
    ADD COLUMN main_warehouse_pallets      SMALLINT        NOT NULL DEFAULT 0 CHECK (main_warehouse_pallets >= 0),
    ADD COLUMN fragile_warehouse_pallets   SMALLINT        NOT NULL DEFAULT 0 CHECK (fragile_warehouse_pallets >= 0),
    ADD COLUMN is_fragile_chemicals        BOOLEAN         NOT NULL DEFAULT FALSE,
    ADD COLUMN is_bulky                    BOOLEAN         NOT NULL DEFAULT FALSE,
    ADD COLUMN notes                       VARCHAR(500),
    ADD COLUMN status                      VARCHAR(50);

-- ------------------------------------------------------------
-- vehicles: personnel mapping
-- ------------------------------------------------------------
ALTER TABLE vehicles
    ADD COLUMN driver_name                 VARCHAR(100);

-- ------------------------------------------------------------
-- Performance Optimization Indexes
-- ------------------------------------------------------------
CREATE INDEX idx_delivery_orders_external ON delivery_orders (external_id)