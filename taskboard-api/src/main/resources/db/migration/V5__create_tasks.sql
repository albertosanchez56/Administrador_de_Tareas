CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'TODO' check (status in ('TODO', 'DOING', 'DONE')),
    position INT NOT NULL DEFAULT 0,
    created_by_user_id BIGINT NOT NULL,
    assigned_to_user_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    due_at TIMESTAMPTZ,

    CONSTRAINT fk_tasks_board_id FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_created_by_user_id FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tasks_assigned_to_user_id FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL
);