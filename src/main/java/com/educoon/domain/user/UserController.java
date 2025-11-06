package com.educoon.domain.user;

import com.educoon.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * 내 정보 조회 API
     * [GET] /api/users/me
     * (포트폴리오: @AuthenticationPrincipal 대신 유틸 클래스 SecurityUtils 사용)
     *
     * @return (DTO) UserProfileResponse
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        // 1. SecurityUtils를 통해 현재 인증된 사용자의 Kakao ID를 가져옵니다.
        String currentKakaoId = SecurityUtils.getCurrentUserKakaoId();

        log.info("내 정보 조회 요청: kakaoId={}", currentKakaoId);

        // 2. UserService를 호출하여 프로필 정보를 DTO로 받습니다.
        UserProfileResponse profileResponse = userService.getUserProfile(currentKakaoId);

        // 3. DTO를 ResponseEntity에 담아 반환합니다.
        return ResponseEntity.ok(profileResponse);
    }
}
