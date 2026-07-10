package com.tareas.taskboard.dto;

import java.time.Instant;

import com.tareas.taskboard.entity.BoardMembers;

public record BoardMemberResponse(
    Long boardId,
    Long userId,
    String role,
    Instant createdAt
) {
    public static BoardMemberResponse from(BoardMembers boardMember) {
        return new BoardMemberResponse(
            boardMember.getBoard().getId(),
            boardMember.getUser().getId(),
            boardMember.getRole().name(),
            boardMember.getCreatedAt()
        );
    }
}
