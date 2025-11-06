package com.educoon.oauth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 클라이언트에서 카카오 액세스 토큰을 받아오기 위한 DTO
 */
@Getter
@NoArgsConstructor
public class KakaoLoginRequest {
    @NotEmpty(message = "카카오 액세스 토큰은 필수입니다")
    private String kakaoAccessToken;
}
