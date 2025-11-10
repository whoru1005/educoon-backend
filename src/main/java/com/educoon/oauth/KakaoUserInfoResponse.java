package com.educoon.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfoResponse {

    private String id;

    private KakaoProperties properties;

    private KakaoAccount kakaoAccount;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoProperties {
        @JsonProperty("nickname")
        private String nickname;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        @JsonProperty("profile")
        private KakaoProfile profile;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoProfile {
        @JsonProperty("profile_image_url")
        private String profileImageUrl;
    }

    // 서비스에서 사용할 편의 메소드
    public String getNickname() {
        return properties != null ? properties.getNickname() : null;
    }

    public String getProfileImageUrl() {
        return (kakaoAccount != null && kakaoAccount.getProfile() != null)
                ? kakaoAccount.getProfile().getProfileImageUrl()
                : null;
    }

}
