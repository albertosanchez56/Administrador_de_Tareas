package com.tareas.taskboard.dto;

import java.time.Instant;

import com.tareas.taskboard.entity.Task;

public record TaskResponse(
    Long id,
    String title,
    String description,
    String status,
    int position,
    Long boardId,
    String createdBy,
    String assignedTo,
    Instant createdAt,
    Instant updatedAt,
    Instant dueAt
) {
    public static TaskResponse fromTask(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus().name(),
            task.getPosition(),
            task.getBoard().getId(),
            task.getCreatedBy().getUsername(),
            task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : null,
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getDueAt()
        );
    }
}
