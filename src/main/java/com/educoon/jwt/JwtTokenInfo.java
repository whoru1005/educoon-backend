package com.educoon.jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtTokenInfo {
    private String grantType;
    private String accessToken;
    private String refreshToken;
}
