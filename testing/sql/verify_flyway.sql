-- Verify Flyway Migration History in PostgreSQL across schemas
-- Ensures that all database tables are versioned and created via Flyway migrations.

-- 1. Check migrations applied in Auth Schema
SELECT installed_rank, version, description, type, script, checksum, execution_time, success 
FROM auth_schema.flyway_schema_history 
ORDER BY installed_rank ASC;

-- 2. Check migrations applied in User Schema
SELECT installed_rank, version, description, type, script, checksum, execution_time, success 
FROM user_schema.flyway_schema_history 
ORDER BY installed_rank ASC;

-- 3. Check migrations applied in Chat Schema
SELECT installed_rank, version, description, type, script, checksum, execution_time, success 
FROM chat_schema.flyway_schema_history 
ORDER BY installed_rank ASC;

-- 4. Check migrations applied in Media Schema
SELECT installed_rank, version, description, type, script, checksum, execution_time, success 
FROM media_schema.flyway_schema_history 
ORDER BY installed_rank ASC;

-- 5. Check migrations applied in Notification Schema
SELECT installed_rank, version, description, type, script, checksum, execution_time, success 
FROM notification_schema.flyway_schema_history 
ORDER BY installed_rank ASC;

-- 6. Check migrations applied in Payment Schema
SELECT installed_rank, version, description, type, script, checksum, execution_time, success 
FROM payment_schema.flyway_schema_history 
ORDER BY installed_rank ASC;

-- 7. Check migrations applied in Admin Schema
SELECT installed_rank, version, description, type, script, checksum, execution_time, success 
FROM admin_schema.flyway_schema_history 
ORDER BY installed_rank ASC;
