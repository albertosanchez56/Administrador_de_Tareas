package com.tareas.taskboard.dto;

import java.time.Instant;

import com.tareas.taskboard.entity.Board;

public record BoardResponse(
    Long id,
    String title,
    String description,
    Long ownerUserId,
    Instant createdAt,
    Instant updatedAt
) {
    public static BoardResponse from(Board board) {
        return new BoardResponse(
            board.getId(),
            board.getTitle(),
            board.getDescription(),
            board.getOwner().getId(),
            board.getCreatedAt(),
            board.getUpdatedAt()
        );
    }
}
