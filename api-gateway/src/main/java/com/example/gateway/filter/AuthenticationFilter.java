package com.example.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    // /api/auth/register is deliberately not public — this is a staff-only system with
    // no self-signup; new users are created by an admin via POST /api/auth/users
    // (see UserController), and the register endpoint itself is now ADMIN-gated too.
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars"
    );

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            // Still strip any client-forged identity headers on public paths (e.g. register/login) —
            // defense in depth, even though today's public endpoints don't consume them.
            ServerWebExchange sanitized = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.remove("X-User-Name");
                        headers.remove("X-User-Role");
                        headers.remove("X-Branch-Id");
                    }))
                    .build();
            return chain.filter(sanitized);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return unauthorized(exchange, "Authentication required");
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = parseToken(token);
            // Forward user identity to downstream services.
            // IMPORTANT: use headers(consumer) + set(...) rather than request(r -> r.header(...)).
            // ServerHttpRequest.Builder#header(name, value) ADDS to any existing values for that
            // header name instead of replacing them — since the mutate() builder starts from a
            // copy of the original (client-supplied) headers, a caller who forges their own
            // X-User-Name/X-User-Role header would have it survive as the *first* value, and
            // downstream services read identity via HttpServletRequest#getHeader(), which
            // returns the first value. That would let a spoofed header (e.g. X-User-Role: ADMIN)
            // win over the value verified here from the JWT — a privilege-escalation hole in the
            // "trust the perimeter" model every downstream service relies on. set(...) overwrites
            // any inbound value instead of appending to it, closing that gap.
            Long branchId = claims.get("branchId", Long.class);
            ServerWebExchange mutated = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.set("X-User-Name", claims.getSubject());
                        headers.set("X-User-Role", claims.get("role", String.class));
                        if (branchId != null) {
                            headers.set("X-Branch-Id", String.valueOf(branchId));
                        } else {
                            headers.remove("X-Branch-Id");
                        }
                    }))
                    .build();
            return chain.filter(mutated);
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage(), e);
            return unauthorized(exchange, "Invalid or expired session");
        }
    }

    // Every downstream service's GlobalExceptionHandler returns a {statusCode, message}
    // JSON body on error (the frontend's ApiErrorBody contract) — matching that here
    // means a 401 rejected at the gateway shows a real message instead of an empty
    // response body, which a browser hitting a protected URL directly would otherwise
    // render as its own bare "page isn't working" error page.
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"statusCode\":401,\"message\":\"%s\"}".formatted(message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private Claims parseToken(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
