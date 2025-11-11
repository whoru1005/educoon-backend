package com.educoon.domain.roomParticipant;

import com.educoon.domain.user.User;
import lombok.Getter;

@Getter
public class RoomParticipantResponse {

    private final Long userId;
    private final String nickname;
    private String profileImageUrl;
    private final boolean isOwner;

    public RoomParticipantResponse(User user, boolean isOwner){
        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.isOwner = isOwner;
    }
}
