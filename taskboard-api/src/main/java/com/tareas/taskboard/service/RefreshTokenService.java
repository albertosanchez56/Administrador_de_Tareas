package com.tareas.taskboard.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.tareas.taskboard.dto.RefreshRequest;
import com.tareas.taskboard.entity.RefreshToken;
import com.tareas.taskboard.entity.User;
import com.tareas.taskboard.repository.RefreshTokenRepository;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTtlDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.security.jwt.refresh-token-ttl-days}") long refreshTtlDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTtlDays = refreshTtlDays;
    }

    public String createRefreshToken(User user, String userAgent, String ip) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        String tokenHash = hashToken(rawToken);
        UUID sessionId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS);

        RefreshToken refreshToken = new RefreshToken(user, sessionId, tokenHash, expiresAt, userAgent, ip);
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public RotatedRefresh rotate(String rawRefreshToken) {
        RefreshToken current = refreshTokenRepository
                .findByTokenHash(hashToken(rawRefreshToken))
                .orElseThrow(() -> new BadCredentialsException("Token de refresco inválido"));

        if (current.getRevokedAt() != null) {
            throw new BadCredentialsException("Token de refresco inválido");
        }
        if (current.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Token de refresco expirado");
        }

        current.setLastUsedAt(Instant.now());

        // Nuevo refresh (mismo sessionId)
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String newRaw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken next = new RefreshToken(
                current.getUser(),
                current.getSessionId(), // mismo session
                hashToken(newRaw),
                Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS),
                current.getUserAgent(),
                current.getIp());
        refreshTokenRepository.save(next);

        current.setRevokedAt(Instant.now());
        current.setReplacedBy(next);
        refreshTokenRepository.save(current);

        return new RotatedRefresh(current.getUser(), newRaw); // el cliente guarda este
    }

    String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return; // nada que hacer
        }
        refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
                .ifPresent(token -> {
                    if (token.getRevokedAt() == null) {
                        token.setRevokedAt(Instant.now());
                        refreshTokenRepository.save(token);
                    }
                });
    }

    public record RotatedRefresh(User user, String rawRefreshToken) {
    }
}
