package com.educoon.domain.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserStatus {

    public enum Status{
        IDLE,
        STUDYING
    }

    private Long userId;

    private String nickname;

    private String profileImageUrl;

    @Builder.Default
    private Status status = Status.IDLE;

    private LocalDateTime studyStartTime;
}
