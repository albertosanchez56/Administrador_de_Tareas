package com.tareas.taskboard.security;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tareas.taskboard.entity.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Service
public class JwtService {

    // Clave secreta usada para firmar y verificar los tokens (HMAC-SHA).
    private final SecretKey secretKey;
    // Tiempo de vida del access token en minutos (lo leo del application.properties).
    private final long ttlMinutes;

    // El secret viene en Base64 desde la variable de entorno -> lo decodifico
    // y construyo la SecretKey. Así no hardcodeo nada en el código.
    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.access-token-ttl-minutes}") long ttlMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.ttlMinutes = ttlMinutes;
    }

    // Genera el access token a partir del usuario.
    // subject = id del usuario, claim "roles" = lista de roles.
    public String generateAccessToken(User user) {

        // now = momento de emisión, exp = now + TTL (en milisegundos).
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMinutes * 60 * 1000);

        return Jwts.builder().subject(String.valueOf(user.getId())).claim("roles", List.of(user.getRole()))
                .issuedAt(now).expiration(exp).signWith(secretKey).compact();

    }

    // Saca el userId del token (lo guardé en el subject como String, lo paso a Long).
    public Long extractUserId(String token) {

        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    // Comprueba si el token es válido: si parseClaims no lanza excepción -> ok.
    // Si la firma no cuadra o está expirado, JJWT lanza JwtException.
    public boolean isTokenValid(String token) {

        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // Devuelve los roles guardados en el claim "roles".
    // Uso List<?> para evitar el unchecked cast y luego mapeo a String.
    public List<String> extractRoles(String token) {
        Claims claims = parseClaims(token);
        List<?> raw = claims.get("roles", List.class);
        if (raw == null)
            return List.of();
        return raw.stream().map(Object::toString).toList();
    }

    // Método privado para no repetir el parseo en cada extract.
    // verifyWith(secretKey) -> aquí es donde se comprueba la firma.
    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
}
