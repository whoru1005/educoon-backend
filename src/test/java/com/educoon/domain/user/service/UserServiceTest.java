package com.educoon.domain.user.service;

import com.educoon.domain.user.dto.UserProfileResponse;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.repository.UserRepository;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("카카오 ID로 사용자 조회 성공")
    void findByKakaoId_Success() {
        // given
        String kakaoId = "12345678";
        User user = User.builder()
                .kakaoId(kakaoId)
                .nickname("테스트유저")
                .build();
        given(userRepository.findByKakaoId(kakaoId)).willReturn(Optional.of(user));

        // when
        User result = userService.findByKakaoId(kakaoId);

        // then
        assertThat(result.getKakaoId()).isEqualTo(kakaoId);
        assertThat(result.getNickname()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("카카오 ID로 사용자 조회 실패 - 존재하지 않는 사용자")
    void findByKakaoId_Fail_UserNotFound() {
        // given
        String kakaoId = "invalid_id";
        given(userRepository.findByKakaoId(kakaoId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findByKakaoId(kakaoId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.KAKAO_USER_INFO_FAILED);
    }

    @Test
    @DisplayName("사용자 프로필 조회 성공")
    void getUserProfile_Success() {
        // given
        String kakaoId = "12345678";
        User user = User.builder()
                .userId(1L)
                .kakaoId(kakaoId)
                .nickname("테스트유저")
                .profileImageUrl("http://test.com/image.jpg")
                .build();
        given(userRepository.findByKakaoId(kakaoId)).willReturn(Optional.of(user));

        // when
        UserProfileResponse result = userService.getUserProfile(kakaoId);

        // then
        assertThat(result.getNickname()).isEqualTo("테스트유저");
        assertThat(result.getProfileImageUrl()).isEqualTo("http://test.com/image.jpg");
    }

    @Test
    @DisplayName("사용자 프로필 조회 실패 - 카카오 ID 누락")
    void getUserProfile_Fail_KakaoIdNull() {
        // when & then
        assertThatThrownBy(() -> userService.getUserProfile(null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.KAKAO_USER_INFO_FAILED);
    }
}
