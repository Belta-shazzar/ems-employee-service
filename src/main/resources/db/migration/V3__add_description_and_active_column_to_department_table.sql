-- 1. Add the new columns (allow NULL temporarily)
ALTER TABLE departments
    ADD COLUMN active BOOLEAN,
    ADD COLUMN description VARCHAR(500);

-- 2. Set all existing rows to active = true
UPDATE departments
SET active = TRUE
WHERE active IS NULL;

-- 3. Enforce NOT NULL only after data is fixed
ALTER TABLE departments
    ALTER COLUMN active SET NOT NULL;

-- 4. Optional: default for future inserts
ALTER TABLE departments
    ALTER COLUMN active SET DEFAULT TRUE;

-- 5. Index (skip if it already exists)
CREATE INDEX IF NOT EXISTS idx_department_name
    ON departments (name);