package com.educoon.security;

import com.educoon.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * API 요청 헤더의 'Authorization'(JWT)을 검증하고
 * SecurityContext에 인증 정보를 등록하는 필터
 * OncePerRequestFilter: 모든 요청마다 한 번만 실행
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer" ;

    private final JwtUtil jwtUtil;


    //jwt 필터 로직
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/ws-stomp/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);


        if (StringUtils.hasText(token)) {
            Authentication authentication = jwtUtil.validateAndGetAuthentication(token);

            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Security Context 인증 저장: {}", authentication.getName());
            } else {
                log.debug("유효한 JWT 토큰이 없습니다.");
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Request Header에서 "Bearer " 접두사를 제거하고 토큰 값만 추출합니다.
     * @param request
     * @return "Bearer "가 제거된 토큰 값 (없으면 null)
     */
    private String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)){
            return bearerToken.substring(7);
        }
        return null;
    }
}
