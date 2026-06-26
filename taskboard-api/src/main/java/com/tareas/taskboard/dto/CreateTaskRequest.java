package com.tareas.taskboard.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(
    @NotBlank String title,
    String description
) {} 