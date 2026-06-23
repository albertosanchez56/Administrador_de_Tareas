package com.tareas.taskboard.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBoardRequest(
    @NotBlank String title,
    String description
){}
