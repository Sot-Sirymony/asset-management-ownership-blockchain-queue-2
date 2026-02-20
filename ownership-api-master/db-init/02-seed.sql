-- Idempotent seed data for local startup convenience.
-- This keeps common reference data available after every run.

INSERT INTO department (dep_name, description)
SELECT 'IT', 'Information Technology'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE dep_name = 'IT'
);

INSERT INTO department (dep_name, description)
SELECT 'Finance', 'Finance and Accounting'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE dep_name = 'Finance'
);

INSERT INTO department (dep_name, description)
SELECT 'HR', 'Human Resources'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE dep_name = 'HR'
);
