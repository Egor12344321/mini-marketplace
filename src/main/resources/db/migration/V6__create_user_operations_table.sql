CREATE TABLE IF NOT EXISTS user_operations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    operation_type VARCHAR(50) NOT NULL CHECK (operation_type IN ('CREATE_ORDER', 'UPDATE_ORDER', 'CANCEL_ORDER')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_operations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_operations_user_id_type_created ON user_operations(user_id, operation_type, created_at DESC);