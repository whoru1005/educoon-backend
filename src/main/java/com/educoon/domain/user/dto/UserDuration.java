package com.educoon.domain.user.dto;

import com.educoon.domain.user.entity.User;
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
