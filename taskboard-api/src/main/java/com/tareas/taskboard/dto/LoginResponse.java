package com.tareas.taskboard.dto;

import com.tareas.taskboard.entity.User;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    Long userId,
    String username,
    String role
){
    public static LoginResponse fromUser(User user, String accessToken, String refreshToken) {
        return new LoginResponse(
            accessToken,
            refreshToken,
            user.getId(),
            user.getUsername(),
            user.getRole()
        );
    }
}
