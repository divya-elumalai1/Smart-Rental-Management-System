-- Sapthagiri Residency — unit tracking + payment mode
ALTER TABLE properties
    ADD COLUMN IF NOT EXISTS unit_number VARCHAR(20),
    ADD COLUMN IF NOT EXISTS floor_label  VARCHAR(50);

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS payment_mode   VARCHAR(30),
    ADD COLUMN IF NOT EXISTS receipt_number VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_properties_unit_number ON properties (unit_number);

-- Set default deposit for existing properties (2x rent amount)
UPDATE properties 
SET deposit = rent_amount * 2 
WHERE deposit IS NULL OR deposit = 0;
