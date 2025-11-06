package com.educoon.domain.user;

import lombok.Builder;
import lombok.Getter;

/**
 * 민감한 정보 제외하고 클라이언트에게 반환할 사용자의 프로필 정보
 */
@Getter
@Builder
public class UserProfileResponse {

    private Long userId;
    private String nickname;
    private String profileImageUrl;

//    User 엔티티를 UserProfileResponse DTO로 변환
    public static UserProfileResponse from(User user){
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
