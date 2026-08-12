-- Verify Payments and Subscriptions in PostgreSQL
-- Used by: payment-service

SET search_path TO payment_schema;

-- 1. Check Payments history
SELECT id, user_id, amount, currency, status, idempotency_key, transaction_id, created_at 
FROM payments 
ORDER BY created_at DESC;

-- 2. Verify Subscriptions state
SELECT id, user_id, plan_id, status, current_period_start, current_period_end, cancel_at_period_end 
FROM subscriptions 
ORDER BY current_period_start DESC;

-- 3. Check Payment Webhook Logs/Audits (if stored)
-- Verify that duplicate webhooks are handled by checking unique transaction constraint violations or idempotency checks.
