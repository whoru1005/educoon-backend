package com.educoon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // TODO: 프론트엔드 개발 환경의 주소(예: http://localhost:5000)로 변경해야 합니다.
    // Flutter Web (Chrome) 실행 시 터미널에 나오는 주소를 확인하세요.
    private static final String FLUTTER_LOCAL_HOST = "http://localhost:5000";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // 모든 HTTP 메서드 허용
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true) // (필요시)
                .maxAge(3600);
    }
}
