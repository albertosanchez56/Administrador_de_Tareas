package com.tareas.taskboard.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Leo el header Authorization (ahí viene el Bearer token si lo envían).
        String authHeader = request.getHeader("Authorization");

        // Solo entro si hay header y empieza por "Bearer ".
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // Quito el prefijo "Bearer " y me quedo solo con el JWT.
            String token = authHeader.substring(7);

            // Valido firma + expiración con el JwtService que ya tengo.
            if (jwtService.isTokenValid(token)) {

                // Saco userId y roles del token (ya tengo métodos para esto).
                Long userId = jwtService.extractUserId(token);
                var roles = jwtService.extractRoles(token);

                // Convierto los roles a authorities de Spring Security.
                // En el token guardo "USER", pero Spring suele usar "ROLE_USER".
                var authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                // Creo el objeto Authentication para esta petición.
                // El principal lo pongo como userId (string) para identificar al usuario.
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userId.toString(),
                        null,
                        authorities);

                // Lo guardo en el contexto -> Spring Security ya sabe quién está autenticado.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Siempre sigo la cadena de filtros, haya token o no.
        filterChain.doFilter(request, response);

    }

}
