ALTER TABLE notifications ADD COLUMN chat_id VARCHAR(36);
CREATE INDEX idx_notifications_recipient_chat ON notifications(recipient_id, chat_id);
