package com.steve.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=u8F3nD9sK2qL1vR5bX7zW6pT4mQ0yA8e"
})
class JwtAuthFilterTest {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private GatewayFilterChain filterChain;

    private static final String SECRET = "u8F3nD9sK2qL1vR5bX7zW6pT4mQ0yA8e";
    private Key signingKey;

    @BeforeEach
    void setUp() {
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    // ── Valid Token ───────────────────────────────────────────────────────────

    @Test
    void filter_allowsRequest_whenValidTokenProvided() {
        String token = generateToken("user@bank.com", 3600000L);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/ACC123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        var gatewayFilter = jwtAuthFilter.apply(new JwtAuthFilter.Config());

        StepVerifier.create(gatewayFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Should NOT have set 401
        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_forwardsUserEmailHeader_whenValidToken() {
        String token = generateToken("steve@bank.com", 3600000L);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/transactions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        var gatewayFilter = jwtAuthFilter.apply(new JwtAuthFilter.Config());

        StepVerifier.create(gatewayFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Verify the X-User-Email header was added for downstream services
        assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Email"))
                .isEqualTo("steve@bank.com");
    }

    // ── Missing Token ─────────────────────────────────────────────────────────

    @Test
    void filter_returns401_whenNoAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/ACC123")
                .build(); // no Authorization header

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        var gatewayFilter = jwtAuthFilter.apply(new JwtAuthFilter.Config());

        StepVerifier.create(gatewayFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_returns401_whenAuthHeaderHasNoBearerPrefix() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/ACC123")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz") // Basic auth
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        var gatewayFilter = jwtAuthFilter.apply(new JwtAuthFilter.Config());

        StepVerifier.create(gatewayFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── Invalid / Expired Token ───────────────────────────────────────────────

    @Test
    void filter_returns401_whenTokenIsExpired() {
        // Generate a token that expired 1 hour ago
        String expiredToken = generateToken("user@bank.com", -3600000L);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/ACC123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        var gatewayFilter = jwtAuthFilter.apply(new JwtAuthFilter.Config());

        StepVerifier.create(gatewayFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_returns401_whenTokenIsCompletelyInvalid() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/ACC123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not.a.real.token")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        var gatewayFilter = jwtAuthFilter.apply(new JwtAuthFilter.Config());

        StepVerifier.create(gatewayFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_returns401_whenTokenSignedWithWrongSecret() {
        // Sign with a different secret
        Key wrongKey = Keys.hmacShaKeyFor(
                "WRONG_SECRET_KEY_THAT_IS_32_CHARS!!".getBytes(StandardCharsets.UTF_8));

        String tokenWithWrongSecret = Jwts.builder()
                .setSubject("user@bank.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000L))
                .signWith(wrongKey, SignatureAlgorithm.HS256)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/accounts/ACC123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithWrongSecret)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        var gatewayFilter = jwtAuthFilter.apply(new JwtAuthFilter.Config());

        StepVerifier.create(gatewayFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String generateToken(String email, long expirationMs) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}