package com.tareas.taskboard.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tareas.taskboard.dto.LoginRequest;
import com.tareas.taskboard.dto.LoginResponse;
import com.tareas.taskboard.repository.UserRepository;
import com.tareas.taskboard.security.JwtService;

import jakarta.transaction.Transactional;

import com.tareas.taskboard.entity.User;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user, null, null);

        return LoginResponse.fromUser(user, accessToken, refreshToken);

    }

    @Transactional
    public LoginResponse refresh(String rawRefreshToken) {
        RefreshTokenService.RotatedRefresh rotated = refreshTokenService.rotate(rawRefreshToken);

        String accessToken = jwtService.generateAccessToken(rotated.user());
        return LoginResponse.fromUser(rotated.user(), accessToken, rotated.rawRefreshToken());
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }
}
