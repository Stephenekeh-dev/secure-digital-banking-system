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

/**
 * JWT validation filter applied to all protected routes via application.yml.
 * Validates the Bearer token and forwards the user's email in X-User-Email header.
 */
@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // Skip JWT check for Swagger and API docs paths
            if (path.contains("/swagger-ui") ||
                    path.contains("/v3/api-docs") ||
                    path.contains("/swagger-resources") ||
                    path.contains("/webjars") ||
                    path.contains("/actuator")) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or malformed Authorization header");
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
                    return unauthorised(exchange);
                }

                ServerWebExchange mutated = exchange.mutate()
                        .request(r -> r.header("X-User-Email", email))
                        .build();

                return chain.filter(mutated);

            } catch (JwtException e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
                return unauthorised(exchange);
            }
        };
    }
    private Mono<Void> unauthorised(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // No config needed currently
    }
}