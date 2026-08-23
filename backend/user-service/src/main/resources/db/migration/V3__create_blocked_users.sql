CREATE TABLE IF NOT EXISTS blocked_users (
    id VARCHAR(36) PRIMARY KEY,
    blocker_id VARCHAR(36) NOT NULL,
    blocked_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_block_pair UNIQUE (blocker_id, blocked_id),
    FOREIGN KEY (blocker_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_blocked_users_blocker ON blocked_users(blocker_id);
