package com.educoon.domain.user;

import lombok.Getter;

@Getter
public class UserDuration {
    private User user;
    private Long totalDuration;

    public UserDuration(User user, Long totalDuration) {
        this.user = user;
        this.totalDuration = (totalDuration != null) ? totalDuration : 0L;
    }
}
