id (PK)
user_id (FK → users.id, NOT NULL)
session_id (UUID, NOT NULL) ← identifica el dispositivo/sesión
token_hash (UNIQUE, NOT NULL)
created_at (NOT NULL)
last_used_at (NOT NULL o NULL)
expires_at (NOT NULL)
revoked_at (NULL)
replaced_by_token_id (FK → refresh_tokens.id, NULL) (útil para rotación)
user_agent (NULL)
ip (NULL)

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_token_id BIGINT,
    user_agent VARCHAR(255),
    ip VARCHAR(255),

    CONSTRAINT fk_refresh_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_refresh_tokens_replaced_by_token_id FOREIGN KEY (replaced_by_token_id) REFERENCES refresh_tokens(id) ON DELETE SET NULL
);