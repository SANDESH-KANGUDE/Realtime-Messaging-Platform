-- Verify Users and Credentials Data in PostgreSQL
-- Used by: auth-service, user-service

-- 1. Verify User Credentials (auth-service database schema)
SET search_path TO auth_schema;
SELECT id, email, phone, role, is_enabled, created_at 
FROM credentials 
ORDER BY created_at DESC;

-- 2. Verify User Profile Information (user-service database schema)
SET search_path TO user_schema;
SELECT id, display_name, bio, profile_picture_url, created_at 
FROM users 
ORDER BY created_at DESC;

-- 3. Verify Blocked User Relationships
SELECT blocker_id, blocked_id, created_at 
FROM blocked_users;

-- 4. Verify User Preferences
SELECT user_id, receive_notifications, dark_mode, language 
FROM user_preferences;
