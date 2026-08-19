-- V5__create_doctors_table.sql

CREATE TABLE doctors (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_doctors_name
    ON doctors(name);

CREATE INDEX idx_doctors_active
    ON doctors(active);

INSERT INTO doctors (id, name, department)
VALUES
    ('MAIN', 'Main Doctor', 'ICU'),
    ('SENIOR', 'Senior Doctor', 'CARDIOLOGY'),
    ('JUNIOR', 'Junior Doctor', 'CARDIOLOGY'),
    ('JUNIOR-2', 'Junior Doctor', 'GENERAL');
