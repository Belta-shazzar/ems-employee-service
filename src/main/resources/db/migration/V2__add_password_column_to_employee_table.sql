ALTER TABLE employees
    ADD password VARCHAR(150);

ALTER TABLE employees
    ALTER COLUMN password SET NOT NULL;