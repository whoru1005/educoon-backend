package com.educoon.controller;

import com.educoon.domain.token.dto.TokenReissueRequest;
import com.educoon.jwt.JwtTokenInfo;
import com.educoon.oauth.AuthService;
import com.educoon.oauth.KakaoLoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 카카오 로그인 API
     * [POST] /api/auth/kakao
     *
     * @param request 카카오 액세스 토큰
     * @return JWT 토큰
     */
    @PostMapping("/kakao")
    public ResponseEntity<JwtTokenInfo> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request){
        log.info("카카오 로그인 요청 수신");

        JwtTokenInfo jwtTokenInfo = authService.loginWithKakao(request.getKakaoAccessToken());

        log.info("로그인 처리 완료");

        return ResponseEntity.ok(jwtTokenInfo);
    }

    @PostMapping("/reissue")
    public ResponseEntity<JwtTokenInfo> reissueToken(@Valid @RequestBody TokenReissueRequest request){
        log.info("토큰 재발급 요청 수신");

        JwtTokenInfo newJwtTokenInfo = authService.reissueToken(request.getRefreshToken());

        log.info("토큰 재발급 처리 완료");

        return ResponseEntity.ok(newJwtTokenInfo);
    }
}
