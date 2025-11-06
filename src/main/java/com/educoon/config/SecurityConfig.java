package com.educoon.config;

import com.educoon.jwt.JwtUtil;
import com.educoon.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
//                1.CSRF 비활성화
//                REST API는 세션을 사용X -> CSRF 보호 필요 X
                .csrf(csrf -> csrf.disable())

//                2.Http Basic 인증 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())

//                3. 폼 로그인 비활성화
                .formLogin(formLogin -> formLogin.disable())

//                4.JWT사용할 것이기에 Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

//                5.HTTP 요청 별 접근 권한 설정
                .authorizeHttpRequests(authz -> authz
//                        /api/auth/** 경로는 모든 사용자에게 허용
                        .requestMatchers("/api/auth/**", "/swagger-ur/**", "/api-docs/**").permitAll()
//                        그 외는 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )

//                6.JWT 필터 추가
//                기본 로그인 필터 실행 전에 실행
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
