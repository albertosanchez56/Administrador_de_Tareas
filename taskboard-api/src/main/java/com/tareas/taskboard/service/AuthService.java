package com.tareas.taskboard.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tareas.taskboard.dto.LoginRequest;
import com.tareas.taskboard.dto.LoginResponse;
import com.tareas.taskboard.repository.UserRepository;
import com.tareas.taskboard.security.JwtService;

import com.tareas.taskboard.entity.User;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByUsername(request.username()).orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String token = jwtService.generateAccessToken(user);

        return LoginResponse.fromUser(user, token);
    }
}
