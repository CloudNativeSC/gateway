package com.cluvy.gateway.filter;

import com.cluvy.gateway.exception.JwtAuthenticationException;
import com.cluvy.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // 예외할 경로 설정
    private static final List<String> EXCLUDE_PATHS = List.of("/api/auth/login/kakao", "/oauth");

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        String method = String.valueOf(request.getMethod());
        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        logger.info("Client Request: {} {}", path, method);

        if (EXCLUDE_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(exchange);

        if (token == null) {
            logger.error("No token found: {}", authorizationHeader);
            return Mono.error(new JwtAuthenticationException("토큰이 없습니다."));
        }

        if (!jwtUtil.validateToken(token)) {
            logger.error("Invalid JWT token: {}", token);
            return Mono.error(new JwtAuthenticationException("유효하지 않은 토큰입니다."));
        }


//        토큰으로 userId를 알아내고 헤더에 붙이는 과정        
//        String userId = jwtUtil.getUsernameFromToken(token);
//        exchange = exchange.mutate()
//                .request(builder -> builder.header("X-User-Id", userId))
//                .build();

        logger.info("JWT Authentication Successed: {} {}", authorizationHeader, path);
        return chain.filter(exchange);
    }

    private String resolveToken(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    public int getOrder() {
        // 필터 실행 순서 (낮을수록 먼저 실행)
        return 0;
    }
}
