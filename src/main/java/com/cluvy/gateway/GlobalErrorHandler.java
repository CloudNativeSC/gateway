package com.cluvy.gateway;

import com.cluvy.gateway.exception.JwtAuthenticationException;
import com.cluvy.gateway.response.ErrorReasonDTO;
import com.cluvy.gateway.response.status.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;

@Slf4j
@Component
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ErrorReasonDTO errorDto;

        // ErrorDTO 생성
        if (ex instanceof JwtAuthenticationException) { // 401 JWT 인증 에러인 경우
            errorDto = ErrorStatus._UNAUTHORIZED.getReasonHttpStatus();
            exchange.getResponse().setStatusCode(errorDto.getHttpStatus());
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        } else if (ex instanceof AccessDeniedException) { // 403
            errorDto = ErrorStatus._FORBIDDEN.getReasonHttpStatus();
            exchange.getResponse().setStatusCode(errorDto.getHttpStatus());
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        } else { // 그 이외인 경우
            errorDto = ErrorStatus._INTERNAL_SERVER_ERROR.getReasonHttpStatus();
            exchange.getResponse().setStatusCode(errorDto.getHttpStatus());
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        }

        // ErrorDTO를 json으로 변환
        DataBuffer buffer;
        try {
            buffer = exchange.getResponse()
                    .bufferFactory()
                    .wrap(objectMapper.writeValueAsBytes(errorDto));
        } catch (Exception e) {
            buffer = exchange.getResponse()
                    .bufferFactory()
                    .wrap("{\"isSuccess\":false,\"code\":\"SERVER_002\",\"message\":\"응답 생성 실패\"}".getBytes());
        }

        logger.info("Send Error: {}", errorDto.getHttpStatus());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
