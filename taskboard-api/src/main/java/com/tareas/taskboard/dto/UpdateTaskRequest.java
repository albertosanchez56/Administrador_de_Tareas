package com.tareas.taskboard.dto;

import java.time.Instant;

import com.tareas.taskboard.entity.Task;

public record UpdateTaskRequest(
    Task.TaskStatus status,
    Long assignedToUserId,
    Instant dueAt
) {
    
}
