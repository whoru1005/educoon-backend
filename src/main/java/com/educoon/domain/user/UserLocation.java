package com.educoon.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLocation {

    private Long userId;

    private String nickname;

    private Long roomId;
}
