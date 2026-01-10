package com.educoon.oauth;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "kakaoApiClient", url = "${spring.security.oauth2.client.provider.kakao.user-info-uri}")
public interface KakaoApiClient {

    @GetMapping
    KakaoUserInfoResponse getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
