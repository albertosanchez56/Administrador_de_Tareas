package com.tareas.taskboard.service;

import java.time.Instant;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tareas.taskboard.dto.ChangePasswordRequest;
import com.tareas.taskboard.dto.RegisterRequest;
import com.tareas.taskboard.dto.UserResponse;
import com.tareas.taskboard.entity.User;
import com.tareas.taskboard.exception.DuplicateEmailException;
import com.tareas.taskboard.exception.DuplicateUsernameException;
import com.tareas.taskboard.exception.UserNotFoundException;
import com.tareas.taskboard.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new DuplicateEmailException("Email already exists " + request.email());
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException("Username already exists " + request.username());
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = new User(
                request.username(),
                request.email().toLowerCase(),
                encodedPassword,
                "USER",
                true,
                false);

        User saved = userRepository.save(user);

        return UserResponse.from(saved);
    }

    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

    }
}
