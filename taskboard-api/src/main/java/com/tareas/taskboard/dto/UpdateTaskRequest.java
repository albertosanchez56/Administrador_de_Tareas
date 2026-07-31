package com.tareas.taskboard.dto;

import java.time.Instant;

import com.tareas.taskboard.entity.Task;

public record UpdateTaskRequest(
    String title,
    String description,
    Task.TaskStatus status,
    Long assignedToUserId,
    Instant dueAt,
    Integer position
) {
    
}
