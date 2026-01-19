package com.educoon.domain.user.controller;

import com.educoon.domain.user.dto.UserProfileResponse;
import com.educoon.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 비활성화 (간단한 테스트를 위해)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("내 프로필 조회 성공")
    @WithMockUser(username = "12345678")
    void getMyProfile_Success() throws Exception {
        // given
        String kakaoId = "12345678";
        UserProfileResponse response = UserProfileResponse.builder()
                .userId(1L)
                .nickname("테스트유저")
                .profileImageUrl("http://test.com/image.jpg")
                .build();
        given(userService.getUserProfile(kakaoId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("테스트유저"))
                .andExpect(jsonPath("$.profileImageUrl").value("http://test.com/image.jpg"));
    }
}
