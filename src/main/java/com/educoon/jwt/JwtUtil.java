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

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {
    private final Key key;
    private static final long ACCESS_TOKEN_EXPITRE_TIME = 30 * 60 * 60 * 1000L;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    //application 파일의 jwt 키를 가져옴
    public JwtUtil(@Value("${jwt.secret.key}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     *
     * @param kakaoId
     * @param authorities
     * @return
     *
     * 카카오 ID와 권한 정보를 받아와서 AccessToken과 RefreshToken 생성
     */
    public JwtTokenInfo generateTokenInfo(String kakaoId, Collection<? extends GrantedAuthority> authorities){


        String authoritiesString = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        long now = (new Date()).getTime();

        String accessToken = Jwts.builder()
                .setSubject(kakaoId)
                .claim("auth", authorities)
                .setExpiration(new Date(now + ACCESS_TOKEN_EXPITRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(kakaoId)
                .setExpiration(new Date(now + ACCESS_TOKEN_EXPITRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtTokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     *
     * @param accessToken
     * @return
     *
     * accessToken을 복호화해 토큰의 정보 기반으로 Authentication 객체 반환
     */
    public Authentication getAuthentication(String accessToken){
        Claims claims = parseClaims(accessToken);

        if(claims.get("auth") == null){
            throw new RuntimeException("권한 정보가 없는 토큰입니다");
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        // UserDetails 객체를 만들어서 Authentication 반환
        // subject를 Long 타입으로 변환
        // 여기서는 예시로 kakaoId(String)를 principal로 사용
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     *
     * @param token
     * @return
     *
     * 토큰 정보 검증
     */
    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJwt(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e){
            log.info("Invalid JWT Token");
        } catch (ExpiredJwtException e){
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e){
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e){
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    /**
     *
     * @param accessToken
     * @return
     *
     * 토큰을 복호화하여 Claims 정보 반환
     */
    private Claims parseClaims(String accessToken){
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJwt(accessToken).getBody();
        } catch (ExpiredJwtException e){
            return e.getClaims();
        }
    }
}
