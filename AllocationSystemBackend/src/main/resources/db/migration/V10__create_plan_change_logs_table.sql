-- Create table for plan change logs
CREATE TABLE IF NOT EXISTS plan_change_logs (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    plan_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    old_value TEXT,
    new_value TEXT,
    event_timestamp TIMESTAMP NOT NULL,
    reason VARCHAR(500)
);

-- Foreign keys
ALTER TABLE plan_change_logs
    ADD CONSTRAINT fk_plan_change_plan FOREIGN KEY (plan_id) REFERENCES allocation_plans (id);

ALTER TABLE plan_change_logs
    ADD CONSTRAINT fk_plan_change_user FOREIGN KEY (user_id) REFERENCES users (id);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_plan_change_plan ON plan_change_logs (plan_id);
CREATE INDEX IF NOT EXISTS idx_plan_change_user ON plan_change_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_plan_change_entity_type ON plan_change_logs (entity_type);
CREATE INDEX IF NOT EXISTS idx_plan_change_timestamp ON plan_change_logs (event_timestamp);
