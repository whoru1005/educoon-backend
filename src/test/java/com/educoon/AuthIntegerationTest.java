package com.educoon;

import com.educoon.jwt.JwtTokenInfo;
import com.educoon.jwt.JwtUtil;
import com.educoon.domain.user.User;
import com.educoon.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Spring Boot 애플리케이션 컨텍스트를 모두 로드 (실제 서버 실행과 동일)
@AutoConfigureMockMvc // MockMvc(가상 API 호출기)를 자동으로 설정
@Transactional // 각 테스트가 끝난 후 DB를 롤백 (DB가 더러워지지 않음)
public class AuthIntegerationTest {

    @Autowired
    private MockMvc mockMvc; // API를 호출하기 위한 도구

    @Autowired
    private UserRepository userRepository; // DB에 테스트 유저를 넣기 위한 도구

    @Autowired
    private JwtUtil jwtUtil; // 테스트용 JWT를 생성하기 위한 도구

    private User testUser;
    private String testUserKakaoId = "TEST_USER_1234";
    private String testUserAccessToken;

    // 각 테스트(@Test)가 실행되기 전에(@BeforeEach) 딱 한 번씩 실행
    @BeforeEach
    void setUp() {
        // 1. 테스트용 유저를 DB에 미리 저장합니다.
        testUser = userRepository.findByNickname("열공하는 철수");

        // 2. 이 유저 정보로 30분짜리 Access Token을 강제로 생성합니다.
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        JwtTokenInfo jwtTokenInfo = jwtUtil.generateTokenInfo(testUserKakaoId, authorities);
        testUserAccessToken = jwtTokenInfo.getAccessToken();

        // [추가] 요청하신 JWT Access Token을 콘솔에 출력합니다.
        System.out.println("====================================================");
        System.out.println("[Test] Generated Access Token:");
        System.out.println(testUserAccessToken);
        System.out.println("====================================================");
    }

    @Test
    @DisplayName("인증(JWT) 없이 /api/users/me 호출 시 401(Unauthorized) 응답")
    void getMyProfile_Without_Token() throws Exception {
        // [When] /api/users/me를 헤더 없이 호출
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                // [Then] 401 상태 코드를 반환하는지 검증
                .andExpect(status().isUnauthorized())
                .andDo(print()); // 요청/응답 상세 출력
    }

    @Test
    @DisplayName("유효한 JWT로 /api/users/me 호출 시 200(OK)과 프로필 응답")
    void getMyProfile_With_Valid_Token() throws Exception {
        // [When] /api/users/me를 유효한 Access Token 헤더와 함께 호출
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + testUserAccessToken) // "Bearer [토큰값]"
                        .contentType(MediaType.APPLICATION_JSON))
                // [Then] 200 OK 상태 코드를 반환하는지 검증
                .andExpect(status().isOk())
                // [And] 응답 JSON의 nickname이 "테스트유저"가 맞는지 검증
                .andExpect(jsonPath("$.nickname").value("테스트유저"))
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()))
                .andDo(print());
    }

    @Test
    @DisplayName("만료된 JWT로 /api/users/me 호출 시 401(Unauthorized) 응답")
    void getMyProfile_With_Expired_Token() throws Exception {
        // [!!] 핵심 수정:
        // 1. 하드코딩된 '가짜' 토큰 대신,
        // 2. '진짜' 키로 서명되었지만 '만료 시간만 과거'인 토큰을 헬퍼로 생성합니다.
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        String expiredToken = jwtUtil.generateExpiredToken(testUserKakaoId, authorities);

        // [추가] 만료된 토큰도 콘솔에 출력합니다.
        System.out.println("====================================================");
        System.out.println("[Test] Generated Expired Token:");
        System.out.println(expiredToken);
        System.out.println("====================================================");

        // [When] 만료된 토큰으로 호출
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON))
                // [Then] 401 상태 코드를 반환하는지 검증
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}