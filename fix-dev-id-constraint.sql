-- Fix dev_id constraint to allow NULL values
-- This allows clients to create projects without assigning a developer

ALTER TABLE projects ALTER COLUMN dev_id DROP NOT NULL;

-- Verify the change
SELECT column_name, is_nullable, data_type 
FROM information_schema.columns 
WHERE table_name = 'projects' AND column_name = 'dev_id';
