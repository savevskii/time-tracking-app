-- 1) Integrity checks
-- End must be after start when present
ALTER TABLE time_entries ADD CONSTRAINT chk_end_after_start CHECK (end_time IS NULL OR end_time >= start_time);

-- Duration must never be negative
ALTER TABLE time_entries ADD CONSTRAINT chk_duration_nonnegative CHECK (duration_minutes >= 0);

-- 2) Indexes for common queries
-- 2.1) findByUserIdOrderByStartTimeDesc
--    WHERE user_id = ?  ORDER BY start_time DESC
CREATE INDEX IF NOT EXISTS idx_te_user_start_desc
    ON time_entries (user_id, start_time DESC);

-- 2.2) topProjectsByMinutes(:from,:to)
--    WHERE start_time in range, GROUP BY project_id
--    Make range scans fast and let SUM() read minutes from index (index-only scans)
CREATE INDEX IF NOT EXISTS idx_te_project_start_desc_inc_minutes
    ON time_entries (project_id, start_time DESC) INCLUDE (duration_minutes);

-- 2.3) sumMinutesBetweenAll(:from,:to)
--    Range on start_time only; INCLUDE duration to help index-only scan on large tables
CREATE INDEX IF NOT EXISTS idx_te_start_time_inc_minutes
    ON time_entries (start_time) INCLUDE (duration_minutes);