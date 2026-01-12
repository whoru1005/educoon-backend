package com.educoon.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
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
public class JwtUtil {

    private final long accessTokenExpireTime;
    private final long refreshTokenExpireTime;

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";

    private final Key key;

    public JwtUtil(@Value("${jwt.secret.key}") String secretKey,
                   @Value("${jwt.access-token-expiration}") long accessTokenExpireTime,
                   @Value("${jwt.refresh-token-expiration}") long refreshTokenExpireTime) {
        this.accessTokenExpireTime = accessTokenExpireTime;
        this.refreshTokenExpireTime = refreshTokenExpireTime;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 유저 정보(kakaoId, 권한)를 받아 Access Token과 Refresh Token을 생성합니다.
     */
    public JwtTokenInfo generateTokenInfo(String kakaoId, Collection<? extends GrantedAuthority> authorities) {
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + accessTokenExpireTime);
        Date refreshTokenExpiresIn = new Date(now + refreshTokenExpireTime);

        String authoritiesString = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 1. Access Token 생성
        String accessToken = Jwts.builder()
                .setSubject(kakaoId)
                .claim(AUTHORITIES_KEY, authoritiesString)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 2. Refresh Token 생성
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
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (claims.get(AUTHORITIES_KEY) == null) {
                log.warn("권한 정보가 없는 토큰입니다.");
                throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
            }

            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            UserDetails principal = new User(claims.getSubject(), "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, token, authorities);

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
        }
    }

    /**
     * 토큰에서 Claims(정보)를 추출합니다.
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (Exception e) {
            log.error("JWT Claims 파싱 중 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Refresh Token에서 Subject(kakaoId)를 추출합니다.
     */
    public String getKakaoIdFromToken(String refreshToken) {
        return parseClaims(refreshToken).getSubject();
    }
}
