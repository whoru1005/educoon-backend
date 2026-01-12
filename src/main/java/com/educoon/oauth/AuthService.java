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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final KakaoApiClient kakaoApiClient;

//    기본 권한
    private static final String USER_ROLE = "ROLE_USER";

    public AuthService(UserRepository userRepository,
                       JwtUtil jwtUtil,
                       RefreshRepository refreshRepository,
                       KakaoApiClient kakaoApiClient) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        this.kakaoApiClient = kakaoApiClient;
    }

    /**
     * 카카오 액세스 토큰 기반으로 로그인/회원가입 처리 후 JWT 발급
     * @param kakaoAccessToken
     * @return TokenInfo
     */
    @Transactional
    public JwtTokenInfo loginWithKakao(String kakaoAccessToken){
        log.info("카카오 로그인 프로세스 시작");
        try {
            KakaoUserInfoResponse userInfo = kakaoApiClient.getUserInfo("Bearer " + kakaoAccessToken);
            log.debug("카카오 사용자 정보 수신 성공: id={}, nickname={}", userInfo.getId(), userInfo.getNickname());

            JwtTokenInfo jwtTokenInfo = processUserLogin(userInfo);
            log.info("카카오 로그인 완료");
            return jwtTokenInfo;
        } catch (feign.FeignException.Unauthorized e) {
            log.error("카카오 인증 실패 (401): {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_KAKAO_TOKEN);
        } catch (Exception e) {
            log.error("카카오 로그인 중 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    protected JwtTokenInfo processUserLogin(KakaoUserInfoResponse userInfo){
        User user = findOrCreateUser(userInfo);

        JwtTokenInfo jwtTokenInfo = jwtUtil.generateTokenInfo(
                user.getKakaoId(),
                Collections.singleton(new SimpleGrantedAuthority(USER_ROLE))
        );

        saveOrUpdateRefreshToken(user, jwtTokenInfo.getRefreshToken());
        log.debug("JWT 토큰 생성 및 Refresh Token 저장 완료: kakaoId={}", user.getKakaoId());

        return jwtTokenInfo;
    }

    private void saveOrUpdateRefreshToken(User user, String refreshTokenValue){
        refreshRepository.findByUser(user)
                .ifPresentOrElse(
                        token -> {
                            log.debug("기존 Refresh Token 갱신: userId={}", user.getUserId());
                            token.updateToken(refreshTokenValue);
                        },
                        () -> {
                            log.debug("새로운 Refresh Token 저장: userId={}", user.getUserId());
                            refreshRepository.save(new RefreshToken(user, refreshTokenValue));
                        }
                );
    }

    @Transactional
    public JwtTokenInfo reissueToken(String refreshTokenValue){
        log.info("토큰 재발급 시작");
        
        // validateAndGetAuthentication에서 유효하지 않으면 CustomException 발생함
        Authentication authentication = jwtUtil.validateAndGetAuthentication(refreshTokenValue);
        
        if(!authentication.isAuthenticated()){
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
            log.error("토큰 소유자가 불일치합니다. DB kakaoId: {}, Token kakaoId: {}",
                    dbRefreshToken.getUser().getKakaoId(), kakaoId);

            throw new CustomException(ErrorCode.MISMATCH_TOKEN_USER);
        }

        User user = dbRefreshToken.getUser();
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(USER_ROLE));
        JwtTokenInfo newJwtTokenInfo = jwtUtil.generateTokenInfo(user.getKakaoId(), authorities);

        log.info("토큰 재발급 성공 - DB Refresh Token 갱신. userId: {}", user.getUserId());
        dbRefreshToken.updateToken(newJwtTokenInfo.getRefreshToken());

        return newJwtTokenInfo;
    }



    /**
     * 카카오 정보 기반으로 DB에서 사용자 찾아 반환
     * 없으면 새로 생성하고 반환
     * @param userInfo 카카오 API 응답 DTO
     * @return User
     */
    private User findOrCreateUser(KakaoUserInfoResponse userInfo) {
        return userRepository.findByKakaoId(userInfo.getId())
                .map(user -> {
                    log.debug("기존 사용자 발견: kakaoId={}", userInfo.getId());
                    return user;
                })
                .orElseGet(() -> {
                    log.info("신규 사용자 생성 완료");
                    User newUser = User.builder()
                            .kakaoId(userInfo.getId())
                            .nickname(userInfo.getNickname())
                            .profileImageUrl(userInfo.getProfileImageUrl())
                            .build();
                    return userRepository.save(newUser);
                });
    }
}
