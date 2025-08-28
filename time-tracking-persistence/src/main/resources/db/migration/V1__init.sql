-- Create schema only if it doesn't exist (safe first run)
CREATE SCHEMA IF NOT EXISTS timetracker;

-- Create tables
CREATE TABLE IF NOT EXISTS projects
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS time_entries
(
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    user_id VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    start_time TIMESTAMPTZ NOT NULL,
    end_time   TIMESTAMPTZ,
    duration_minutes INTEGER
);
