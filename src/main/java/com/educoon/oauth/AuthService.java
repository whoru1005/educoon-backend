package com.educoon.oauth;

import com.educoon.domain.user.User;
import com.educoon.domain.user.UserRepository;
import com.educoon.jwt.JwtTokenInfo;
import com.educoon.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Service
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final WebClient.Builder webClientBuilder;
    private final String KAKAO_USER_INFO_URI;

//    기본 권한
    private static final String USER_ROLE = "ROLE_USER";

    public AuthService(UserRepository userRepository,
                       JwtUtil jwtUtil,
                       WebClient.Builder webClientBuilder,
                       @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}") String KAKAO_USER_INFO_URI) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.webClientBuilder = webClientBuilder;
        this.KAKAO_USER_INFO_URI = KAKAO_USER_INFO_URI;

        log.info("Kakao User Info URI : {}", this.KAKAO_USER_INFO_URI);
    }

    /**
     * 카카오 액세스 토큰 기반으로 로그인/회원가입 처리 후 JWT 발급
     * @param kakaoAccessToken
     * @return TokenInfo
     */
    public JwtTokenInfo loginWithKakao(String kakaoAccessToken){

//        1. 카카오 API를 호출해 사용자 정보를 가져옴
        KakaoUserInfoResponse userInfoResponse = getKakaoUserInfo(kakaoAccessToken);

        log.debug("카카오 사용자 정보: kakaoId={}, nickname={}", userInfoResponse.getKakaoId(), userInfoResponse.getNickname());

//        2.카카오 ID 기반으로 사용자를 조회하거나, 없으면 새로 생성
        User user = findOrCreateUser(userInfoResponse);

//        3.JWT 발급
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(USER_ROLE));

        return jwtUtil.generateTokenInfo(user.getKakaoId(), authorities);
    }

    /**
     * WebClient로 카카오 서버와 통신, 사용자 정보 가져옴
     * @param token 카카오 액세스 토큰
     * @return KakaoUserInfoResponse
     */
    private KakaoUserInfoResponse getKakaoUserInfo(String token){
        WebClient webClient = webClientBuilder
                .baseUrl(this.KAKAO_USER_INFO_URI)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();

//        비동기 처리
//        .block()로 동기식으로 결과 대기
        try{
            return webClient.get()
                    .uri("")
//                    응답 가져옴
                    .retrieve()
//                    KakaoUserInfoResponse DTO로 변환
                    .bodyToMono(KakaoUserInfoResponse.class)
//                    비동기 Mono가 완료될 때까지 대기
                    .block();
        }catch (Exception e){
            log.error("카카오 사용자 정보 요청 실패:{}", e.getMessage());
            throw new RuntimeException("카카오 서버로부터 사용자 정보를 가져오는데 실패했습니다.", e);
        }
    }

    /**
     * 카카오 정보 기반으로 DB에서 사용자 찾아 반환
     * 없으면 새로 생성하고 반환
     * @param userInfo 카카오 API 응답 DTO
     * @return User
     */
    private User findOrCreateUser(KakaoUserInfoResponse userInfo){
        return userRepository.findByKakaoId(userInfo.getKakaoId())
                .orElseGet(() -> {
                    log.info("새로운 사용자 회원 가입: kakaoId = {}", userInfo.getKakaoId());

                    User newUser = User.builder()
                            .kakaoId(userInfo.getKakaoId())
                            .nickname(userInfo.getKakaoId())
                            .profileImageUrl(userInfo.getProfileImageUrl())
                            .build();

                    return userRepository.save(newUser);
                });
    }
}
