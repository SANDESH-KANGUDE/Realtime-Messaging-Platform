-- Verify Friend Requests and Friendships in PostgreSQL
-- Used by: user-service

SET search_path TO user_schema;

-- 1. Check all friend requests and their current statuses
SELECT id, sender_id, receiver_id, status, created_at, updated_at 
FROM friend_requests 
ORDER BY created_at DESC;

-- 2. Verify count of friend requests by status
SELECT status, COUNT(*) 
FROM friend_requests 
GROUP BY status;

-- 3. Check established friend connections
SELECT user_id, friend_id, created_at 
FROM friendships 
ORDER BY created_at DESC;
