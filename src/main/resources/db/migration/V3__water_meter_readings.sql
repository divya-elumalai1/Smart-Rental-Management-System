-- Water Meter Readings for Sapthagiri Residency
CREATE TABLE IF NOT EXISTS water_meter_readings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id     UUID NOT NULL REFERENCES properties (id),
    unit_number     VARCHAR(20) NOT NULL,
    previous_reading NUMERIC(10,2) NOT NULL DEFAULT 0,
    current_reading NUMERIC(10,2) NOT NULL,
    units_consumed NUMERIC(10,2) NOT NULL,
    reading_date   DATE NOT NULL,
    meter_photo_url VARCHAR(500),
    notes           VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_water_meter_property_id ON water_meter_readings(property_id);
CREATE INDEX IF NOT EXISTS idx_water_meter_unit_number ON water_meter_readings(unit_number);
CREATE INDEX IF NOT EXISTS idx_water_meter_reading_date ON water_meter_readings(reading_date);
