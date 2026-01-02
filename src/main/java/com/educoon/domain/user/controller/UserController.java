package com.educoon.domain.user.controller;

import com.educoon.domain.user.dto.UserProfileResponse;
import com.educoon.domain.user.service.UserService;
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
     * @return UserProfileResponse(id, nickname, profileUrl)
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {

        String currentKakaoId = SecurityUtils.getCurrentUserKakaoId();

        log.info("내 정보 조회 요청: kakaoId={}", currentKakaoId);

        UserProfileResponse profileResponse = userService.getUserProfile(currentKakaoId);

        return ResponseEntity.ok(profileResponse);
    }
}
