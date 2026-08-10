package com.tareas.taskboard.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBoardRequest (
    @NotBlank String title,
    String description
){}

