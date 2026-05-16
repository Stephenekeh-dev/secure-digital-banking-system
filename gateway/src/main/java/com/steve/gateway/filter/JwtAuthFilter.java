package com.steve.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    // All paths that should skip JWT validation
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/",
            "/api/auth",
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/actuator",
            "/docs",
            "/auth-service",
            "/account-service",
            "/transaction-service",
            "/notification-service",
            "/audit-service",
            "/fraud-service",
            "/approval-service"
    );

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            log.debug("JwtAuthFilter processing path: {}", path);

            // Check if path is public
            boolean isPublic = PUBLIC_PATHS.stream()
                    .anyMatch(path::startsWith);

            if (isPublic) {
                log.debug("Public path, skipping JWT validation: {}", path);
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                return unauthorised(exchange);
            }

            String token = authHeader.substring(7);

            try {
                Key key = Keys.hmacShaKeyFor(
                        secret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String email = claims.getSubject();
                if (email == null) {
                    log.warn("JWT has no subject for path: {}", path);
                    return unauthorised(exchange);
                }

                log.debug("JWT valid for email: {} on path: {}", email, path);

                ServerWebExchange mutated = exchange.mutate()
                        .request(r -> r.header("X-User-Email", email))
                        .build();

                return chain.filter(mutated);

            } catch (JwtException e) {
                log.warn("Invalid JWT for path {}: {}", path, e.getMessage());
                return unauthorised(exchange);
            }
        };
    }

    private Mono<Void> unauthorised(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
    }
}