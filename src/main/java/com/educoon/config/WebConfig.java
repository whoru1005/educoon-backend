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
        registry.addMapping("/api/**") // /api/.. 로 시작하는 모든 경_
                .allowedOrigins(FLUTTER_LOCAL_HOST) // Flutter 로컬 주소만 허용
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 허용할 HTTP
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true) // 쿠키/인증 정보 허용
                .maxAge(3600); // 1시간 동안 pre-flight
    }
}
