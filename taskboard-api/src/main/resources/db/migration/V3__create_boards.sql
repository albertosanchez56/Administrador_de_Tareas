CREATE TABLE boards (
    id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_boards_owner_user_id FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE RESTRICT
);


CREATE INDEX idx_boards_owner_user_id ON boards(owner_user_id);