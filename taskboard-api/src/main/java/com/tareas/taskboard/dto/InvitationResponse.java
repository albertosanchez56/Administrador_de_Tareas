package com.tareas.taskboard.dto;

import java.time.Instant;

import com.tareas.taskboard.entity.BoardInvitation;

public record InvitationResponse(
    Long id,
    Long boardId,
    String boardTitle,
    Long inviteeUserId,
    String inviteeUsername,
    String invitedByUsername,
    String status,
    Instant createdAt
) {
    public static InvitationResponse from(BoardInvitation invitation) {
        return new InvitationResponse(
            invitation.getId(),
            invitation.getBoard().getId(),
            invitation.getBoard().getTitle(),
            invitation.getInvitee().getId(),
            invitation.getInvitee().getUsername(),
            invitation.getInvitedBy().getUsername(),
            invitation.getStatus().name(),
            invitation.getCreatedAt()
        );
    }
}
