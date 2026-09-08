CREATE TABLE board_invitations (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT NOT NULL,
    invitee_user_id BIGINT NOT NULL,
    invited_by_user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_board_invitations_board_id
        FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_board_invitations_invitee_user_id
        FOREIGN KEY (invitee_user_id) REFERENCES users(id),
    CONSTRAINT fk_board_invitations_invited_by_user_id
        FOREIGN KEY (invited_by_user_id) REFERENCES users(id)
);

-- Evita dos invitaciones PENDING al mismo usuario en el mismo tablero
CREATE UNIQUE INDEX uq_board_invitations_pending
    ON board_invitations (board_id, invitee_user_id)
    WHERE status = 'PENDING';

CREATE INDEX idx_board_invitations_invitee_status
    ON board_invitations (invitee_user_id, status);

CREATE INDEX idx_board_invitations_board_id
    ON board_invitations (board_id);