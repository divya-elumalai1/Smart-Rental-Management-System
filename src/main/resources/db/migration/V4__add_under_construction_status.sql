-- Add UNDER_CONSTRUCTION to properties status check constraint
-- This allows units to be marked as under construction

-- Drop the old constraint
ALTER TABLE properties DROP CONSTRAINT IF EXISTS properties_status_check;

-- Add the new constraint with UNDER_CONSTRUCTION included
ALTER TABLE properties 
ADD CONSTRAINT properties_status_check 
CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'UNDER_CONSTRUCTION'));
