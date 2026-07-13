package com.tareas.taskboard.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(
    @NotBlank String title,
    String description,
    Long assignedToUserId,
    Instant dueAt
) {} 