package com.tareas.taskboard.dto;

import java.time.Instant;

import com.tareas.taskboard.entity.User;

public record UserResponse(
    Long id,
    String username,
    String email,
    String role,
    boolean enabled,
    Instant createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.isEnabled(),
            user.getCreatedAt()
        );
    }
}
