package com.educoon.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security 컨텍스트에서 현재 인증된 사용자의 정보를 가져오기 위한 유틸 클래스
 * @AutehnticationPrincipal까지 가지 않고 컨트롤러나 서비스에서 바로 사용할 수 있게 모듈화
 */
@Slf4j
public class SecurityUtils {

//    인스턴스화 방지
    private SecurityUtils(){
    }

    /**
     * 현재 인증 된 사용자의 KakaoID 반환
     * @return String (Kakao ID)
     */
    public static String getCurrentUserKakaoId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || authentication.getPrincipal() == null){
            log.warn("Security에 인증 정보가 없습니다");
            return null;
        }

        if(authentication.getPrincipal() instanceof UserDetails){
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.debug("현재 인증된 사용자 정보: {}", userDetails.getUsername());
            return userDetails.getUsername();
        }

        if(authentication.getPrincipal() instanceof String){
            return (String) authentication.getPrincipal();
        }

        log.warn("알 수 없는 타입의 Principal: {}", authentication.getPrincipal().getClass());
        return null;
    }
}
