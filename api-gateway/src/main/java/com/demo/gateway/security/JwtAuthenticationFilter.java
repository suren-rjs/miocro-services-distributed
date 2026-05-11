package com.demo.gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip filtering for OPTIONS requests (CORS pre-flight)
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        // Skip filtering for auth endpoints
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        // Only filter /api/** requests
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            // Fallback to query param for SSE (EventSource doesn't support headers)
            token = request.getQueryParams().getFirst("token");
        }

        if (token == null) {
            log.warn("Unauthorized access attempt to {}: Missing token", path);
            return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);
        }

        if (!jwtUtils.validateToken(token)) {
            log.warn("Unauthorized access attempt to {}: Invalid JWT token", path);
            return onError(exchange, "Invalid Token", HttpStatus.UNAUTHORIZED);
        }

        // Optional: Add user info to headers for downstream services
        String username = jwtUtils.getUsernameFromToken(token);
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Auth-User", username)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
}
