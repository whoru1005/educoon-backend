package com.educoon.domain.user.dto;

import com.educoon.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

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
