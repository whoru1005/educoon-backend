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

    // 7일 (데모 버전)
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;
    // 14일
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 14 * 24 * 60 * 60 * 1000L;

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";

    private final Key key;

    // application-secret.yml 에 정의된 jwt.secret.key 값을 가져옴
    public JwtUtil(@Value("${jwt.secret.key}") String secretKey) {
        // [!!] 핵심 수정:
        // Base64 디코딩(Decoders.BASE64.decode) 대신,
        // 문자열의 UTF-8 바이트를 그대로 키로 사용합니다.
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
                .setSubject(kakaoId) // [!!] 재발급 시 kakaoId 식별을 위해 추가
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtTokenInfo.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * JWT 토큰을 복호화하여 토큰에 들어있는 권한(Authentication) 정보를 꺼냅니다.
     */
    public Authentication getAuthentication(String accessToken) {
        // 1. 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 2. 클레임에서 권한 정보(auth) 추출
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // 3. UserDetails 객체를 만들어서 Authentication 반환
        // (여기서 UserDetails의 'username'에 JWT의 'subject'(kakaoId)를 넣습니다)
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * 토큰 정보를 검증합니다.
     */
    public boolean validateToken(String token) {
        try {
            // [!!] 수정된 key로 검증 시도
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            // [!!] 여기가 현재 문제 지점입니다.
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    // [!!] 테스트 코드를 위한 헬퍼(Helper) 메소드 추가
    /**
     * (테스트용) 만료된 Access Token을 생성합니다.
     */
    public String generateExpiredToken(String kakaoId, Collection<? extends GrantedAuthority> authorities) {
        long now = (new Date()).getTime();
        // 1. 만료 시간을 1시간 전으로 설정
        Date expiredAccessTokenExpiresIn = new Date(now - 3600 * 1000L);

        return Jwts.builder()
                .setSubject(kakaoId)
                .claim(AUTHORITIES_KEY, authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")))
                .setExpiration(expiredAccessTokenExpiresIn) // [!!] 과거 시간
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
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
