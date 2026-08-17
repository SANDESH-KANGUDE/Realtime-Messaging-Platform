-- Verify Transactional Outbox State in PostgreSQL
-- Used across services: auth-service, user-service, chat-service, media-service

-- 1. Check Outbox Table Content in Auth Schema
SET search_path TO auth_schema;
SELECT id, event_type, status, attempts, created_at, processed_at 
FROM outbox_events 
ORDER BY created_at DESC 
LIMIT 20;

-- 2. Count Outbox Events by Status in Auth Schema
SELECT status, COUNT(*) 
FROM outbox_events 
GROUP BY status;

-- 3. Check Pending Events (events that haven't been published to Kafka yet)
SELECT id, event_type, payload 
FROM outbox_events 
WHERE status = 'PENDING' 
ORDER BY created_at ASC;

-- 4. Check Outbox in User Schema
SET search_path TO user_schema;
SELECT id, event_type, status, attempts, created_at, processed_at 
FROM outbox_events 
ORDER BY created_at DESC 
LIMIT 20;

-- 5. Check Outbox in Chat Schema
SET search_path TO chat_schema;
SELECT id, event_type, status, attempts, created_at, processed_at 
FROM outbox_events 
ORDER BY created_at DESC 
LIMIT 20;
