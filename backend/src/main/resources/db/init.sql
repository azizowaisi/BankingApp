-- Database initialization script
-- This script runs automatically when PostgreSQL container starts for the first time
-- Tables are created automatically by JPA/Hibernate (ddl-auto: update)

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set timezone
SET timezone = 'UTC';

-- Create database if it doesn't exist (handled by POSTGRES_DB env var)
-- But we can verify it exists
SELECT 'Database banking_db initialized successfully' AS status;

-- Note: Tables are auto-created by Hibernate JPA from entity classes:
-- - users
-- - accounts
-- - transactions
-- - audit_logs
-- - fraud_events

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE banking_db TO banking_user;

-- Note: Additional indexes and optimizations can be added here
-- Performance indexes (uncomment if needed):
-- CREATE INDEX IF NOT EXISTS idx_transactions_timestamp ON transactions(timestamp DESC);
-- CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp DESC);
-- CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
-- CREATE INDEX IF NOT EXISTS idx_transactions_sender ON transactions(sender_account_id);
-- CREATE INDEX IF NOT EXISTS idx_transactions_receiver ON transactions(receiver_account_id);

