package com.educoon.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil { // 사용자님의 클래스명

    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRE_TIME;
    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRE_TIME;

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";

    private final Key key;

    public JwtUtil(@Value("${jwt.secret.key}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 유저 정보(kakaoId, 권한)를 받아 Access Token과 Refresh Token을 생성합니다.
     */
    public JwtTokenInfo generateTokenInfo(String kakaoId, Collection<? extends GrantedAuthority> authorities) {
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        Date refreshTokenExpiresIn = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);

        // 1. Access Token 생성
        String accessToken = Jwts.builder()
                .setSubject(kakaoId)
                .claim(AUTHORITIES_KEY, authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")))
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 2. Refresh Token 생성 (별도 정보 없이, 만료 시간만 길게)
        String refreshToken = Jwts.builder()
                .setSubject(kakaoId)
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtTokenInfo.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public Authentication validateAndGetAuthentication(String token) {
        try {
            // 1. 파싱 (여기서 서명 검증, 만료 체크 다 일어남)
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 2. 권한 정보 추출
            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(claims.get("auth").toString().split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            UserDetails principal = new User(claims.getSubject(), "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, token, authorities);

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.");
            // 필요하다면 여기서 CustomException을 던져 필터에서 잡아서 처리 가능
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다.");
        }
        return null; // 유효하지 않으면 null 반환
    }

    /**
     * 토큰에서 Claims(정보)를 추출합니다.
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이라도 정보를 꺼내야 할 때가 있으므로, 예외적으로 Claims 반환
            return e.getClaims();
        }
    }

    /**
     * Refresh Token에서 Subject(kakaoId)를 추출합니다.
     */
    public String getKakaoIdFromToken(String refreshToken) {
        return parseClaims(refreshToken).getSubject();
    }
}
