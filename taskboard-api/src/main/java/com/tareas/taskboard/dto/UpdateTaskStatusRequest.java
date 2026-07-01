package com.tareas.taskboard.dto;

import com.tareas.taskboard.entity.Task;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
    @NotNull Task.TaskStatus status
) {
} 
