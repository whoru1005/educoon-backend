package com.educoon.oauth;

import com.educoon.domain.token.repository.RefreshRepository;
import com.educoon.domain.token.entity.RefreshToken;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.repository.UserRepository;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import com.educoon.jwt.JwtTokenInfo;
import com.educoon.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final WebClient webclient;
    private final String KAKAO_USER_INFO_URI;

//    기본 권한
    private static final String USER_ROLE = "ROLE_USER";

    public AuthService(UserRepository userRepository,
                       JwtUtil jwtUtil,
                       RefreshRepository refreshRepository,
                       WebClient.Builder webClientBuilder,
                       @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}") String KAKAO_USER_INFO_URI) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        this.webclient = webClientBuilder.baseUrl(KAKAO_USER_INFO_URI).build();
        this.KAKAO_USER_INFO_URI = KAKAO_USER_INFO_URI;
    }

    /**
     * 카카오 액세스 토큰 기반으로 로그인/회원가입 처리 후 JWT 발급
     * @param kakaoAccessToken
     * @return TokenInfo
     */
    public JwtTokenInfo loginWithKakao(String kakaoAccessToken){

        KakaoUserInfoResponse userInfo = getKakaoUserInfo(kakaoAccessToken);

        return processUserLogin(userInfo);
    }

    @Transactional
    protected JwtTokenInfo processUserLogin(KakaoUserInfoResponse userInfo){

        User user = findOrCreateUser(userInfo);

        JwtTokenInfo jwtTokenInfo = jwtUtil.generateTokenInfo(
                user.getKakaoId(),
                Collections.singleton(new SimpleGrantedAuthority(USER_ROLE))
        );

        saveOrUpdateRefreshToken(user, jwtTokenInfo.getRefreshToken());

        return jwtTokenInfo;
    }

    private void saveOrUpdateRefreshToken(User user, String refreshTokenValue){
        refreshRepository.findByUser(user)
                .ifPresentOrElse(
                        token -> token.updateToken(refreshTokenValue),
                        () -> refreshRepository.save(new RefreshToken(user, refreshTokenValue))
                );
    }

    @Transactional
    public JwtTokenInfo reissueToken(String refreshTokenValue){
        if(!jwtUtil.validateAndGetAuthentication(refreshTokenValue).isAuthenticated()){
            log.warn("유효하지 않은 Refresh Token: {}", refreshTokenValue);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String kakaoId = jwtUtil.getKakaoIdFromToken(refreshTokenValue);

        RefreshToken dbRefreshToken = refreshRepository.findByTokenValue(refreshTokenValue)
                .orElseThrow(() ->{
                    log.warn("DB에 존재하지 않는 Refresh Token: {}", refreshTokenValue);
                    return new CustomException(ErrorCode.NOT_FOUND_REFRESH_TOKEN);
                });

        if(!dbRefreshToken.getUser().getKakaoId().equals(kakaoId)){
            log.error("토큰 소유자가 불일치합니다. DB: {}, Token:{}",
                    dbRefreshToken.getUser().getKakaoId(), kakaoId);

            throw new CustomException(ErrorCode.MISMATCH_TOKEN_USER);
        }

        User user = dbRefreshToken.getUser();
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(USER_ROLE));
        JwtTokenInfo newJwtTokenInfo = jwtUtil.generateTokenInfo(user.getKakaoId(), authorities);

        log.debug("토큰 재발급 - DB Refresh Token 갱신. userId: {}", user.getUserId());
        dbRefreshToken.updateToken(newJwtTokenInfo.getRefreshToken());

        return newJwtTokenInfo;
    }

    /**
     * WebClient로 카카오 서버와 통신, 사용자 정보 가져옴
     * @param token 카카오 액세스 토큰
     * @return KakaoUserInfoResponse
     */
    private Mono<KakaoUserInfoResponse> getKakaoUserInfo(String token) {
        return webclient.get()
                .uri("")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponse.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorMap(e -> {
                    log.error("Kakao Login Failed: {}", e.getMessage());
                    return new RuntimeException("카카오 로그인 중 오류가 발생했습니다");
                });
    }


    /**
     * 카카오 정보 기반으로 DB에서 사용자 찾아 반환
     * 없으면 새로 생성하고 반환
     * @param userInfo 카카오 API 응답 DTO
     * @return User
     */
    private User findOrCreateUser(KakaoUserInfoResponse userInfo) {
        return userRepository.findByKakaoId(userInfo.getId())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .kakaoId(userInfo.getId())
                            .nickname(userInfo.getNickname())
                            .profileImageUrl(userInfo.getProfileImageUrl())
                            .build();
                    return userRepository.save(newUser);
                });
    }
}
