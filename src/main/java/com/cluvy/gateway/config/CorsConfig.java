package com.cluvy.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000"); // 허용할 Origin
        config.addAllowedMethod("*"); // 모든 HTTP 메서드 허용 (GET, POST 등)
        config.addAllowedHeader("*"); // 모든 헤더 허용
        config.setAllowCredentials(true); // 쿠키를 포함한 인증 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 CORS 설정 적용
        return source;
    }

    @Bean
    public WebFilter corsFilter(CorsConfigurationSource corsConfigurationSource) {
        return new org.springframework.web.cors.reactive.CorsWebFilter(corsConfigurationSource);
    }
}