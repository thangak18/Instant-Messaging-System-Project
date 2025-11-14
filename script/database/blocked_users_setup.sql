-- =====================================================
-- BLOCKED USERS TABLE SETUP
-- Bảng lưu danh sách users bị block
-- =====================================================

-- Drop table if exists
DROP TABLE IF EXISTS blocked_users CASCADE;

-- Create blocked_users table
CREATE TABLE blocked_users (
    block_id SERIAL PRIMARY KEY,
    blocker_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    blocked_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    blocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT check_different_users CHECK (blocker_id != blocked_id),
    CONSTRAINT unique_block UNIQUE (blocker_id, blocked_id)
);

-- Create indexes for better performance
CREATE INDEX idx_blocked_users_blocker ON blocked_users(blocker_id);
CREATE INDEX idx_blocked_users_blocked ON blocked_users(blocked_id);

-- Add comments
COMMENT ON TABLE blocked_users IS 'Danh sách users bị block';
COMMENT ON COLUMN blocked_users.blocker_id IS 'User thực hiện block';
COMMENT ON COLUMN blocked_users.blocked_id IS 'User bị block';
COMMENT ON COLUMN blocked_users.blocked_at IS 'Thời gian block';

-- Sample data (optional)
-- INSERT INTO blocked_users (blocker_id, blocked_id) 
-- VALUES (1, 2);

GRANT SELECT, INSERT, DELETE ON blocked_users TO postgres;
GRANT USAGE, SELECT ON SEQUENCE blocked_users_block_id_seq TO postgres;

-- Verify table
SELECT * FROM blocked_users;
