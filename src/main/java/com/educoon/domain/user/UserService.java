package com.educoon.domain.user;

import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    /**
     * Kakao ID를 기반으로 사용자를 조회
     *
     * @param kakaoId
     * @return User
     */
    public User findByKakaoId(String kakaoId){
        return userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.KAKAO_USER_INFO_FAILED));
    }


    /**
     * 현재 로그인된 사용자의 프로필 정보를 DTO로 반환
     *
     * @param kakaoId SecurityUtils에서 꺼내온 현재 사용자의 Kakao ID
     * @return UserProfileResponse DTO
     */
    public UserProfileResponse getUserProfile(String kakaoId){
        if(kakaoId == null){
            throw new CustomException(ErrorCode.KAKAO_USER_INFO_FAILED);
        }

        User user = findByKakaoId(kakaoId);

        return UserProfileResponse.from(user);
    }
}
