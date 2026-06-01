package com.tareas.taskboard.dto;

import com.tareas.taskboard.entity.User;

public record LoginResponse(
    String accessToken,
    Long userId,
    String username,
    String role
){
    public static LoginResponse fromUser(User user, String accessToken) {
        return new LoginResponse(
            accessToken,
            user.getId(),
            user.getUsername(),
            user.getRole()
        );
    }
}
