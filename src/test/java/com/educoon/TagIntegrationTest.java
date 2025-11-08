package com.educoon;


import com.educoon.domain.department.Department;
import com.educoon.domain.department.DepartmentRepository;
import com.educoon.domain.tag.Tag;
import com.educoon.domain.tag.TagCreateRequest;
import com.educoon.domain.tag.TagRepository;
import com.educoon.domain.tagCategory.TagCategory;
import com.educoon.domain.user.User;
import com.educoon.domain.user.UserRepository;
import com.educoon.jwt.JwtTokenInfo;
import com.educoon.jwt.JwtUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TagIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper; // DTO를 JSON으로 변환하기 위해 필요

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private TagRepository tagRepository;

    private String testUserAccessToken;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        // 1. 인증(JWT)을 위한 테스트 유저 생성 및 토큰 발급
        User testUser = User.builder()
                .kakaoId("tag-test-kakao-123")
                .nickname("태그테스터")
                .profileImageUrl("http://test.image.com/img.png")
                .build();
        userRepository.save(testUser);

        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        JwtTokenInfo jwtTokenInfo = jwtUtil.generateTokenInfo(testUser.getKakaoId(), authorities);
        testUserAccessToken = jwtTokenInfo.getAccessToken();

        // 2. [API 1] 테스트를 위한 학과(Department) 마스터 데이터 생성
        testDepartment = Department.builder()
                .name("컴퓨터공학부")
                .build();
        departmentRepository.save(testDepartment);
    }

    @Test
    @DisplayName("[API 1] 학과 목록 조회 (GET /api/tags/departments) 성공")
    void getDepartments_Success() throws Exception {
        // [When]
        mockMvc.perform(get("/api/tags/departments")
                        .header("Authorization", "Bearer " + testUserAccessToken) // 인증 토큰
                        .contentType(MediaType.APPLICATION_JSON))
                // [Then]
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // 1개가 조회되어야 함
                .andExpect(jsonPath("$[0].name").value("컴퓨터공학부"))
                .andExpect(jsonPath("$[0].departmentId").value(testDepartment.getDepartment_id()))
                .andDo(print());
    }

    @Test
    @DisplayName("[API 2] 신규 '교외' 태그 생성 (POST /api/tags) 성공 (Create)")
    void createTag_New_Suburban_Success() throws Exception {
        // [Given] "토익" (교외) 태그 DTO
        TagCreateRequest request = new TagCreateRequest();
        // (실제 DTO에 @Setter가 없으므로, 리플렉션이나 @AllArgsConstructor 등을 사용해야 함)
        // (가정: DTO에 Setter가 있거나, 아래와 같이 수동으로 JSON 생성)
        String jsonRequest = objectMapper.writeValueAsString(
                new TagCreateRequest("토익", TagCategory.SUBURBAN, null)
        );

        // [When]
        mockMvc.perform(post("/api/tags")
                        .header("Authorization", "Bearer " + testUserAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                // [Then]
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(jsonPath("$.name").value("토익"))
                .andExpect(jsonPath("$.tagcategory").value("SUBURBAN"))
                .andDo(print());
    }

    @Test
    @DisplayName("[API 2] 기존 '교외' 태그 조회 (POST /api/tags) 성공 (Find)")
    void createTag_Find_Existing_Tag_Success() throws Exception {
        // [Given] 1. "사회" (교양) 태그를 미리 DB에 저장
        Tag existingTag = Tag.builder()
                .name("사회")
                .category(TagCategory.LIBERAL_ARTS)
                .build();
        tagRepository.save(existingTag);

        // [Given] 2. "사회" (교양) 태그를 생성하려는 DTO
        String jsonRequest = objectMapper.writeValueAsString(
                new TagCreateRequest("사회", TagCategory.LIBERAL_ARTS, null)
        );

        // [When] "Find or Create" API 호출
        mockMvc.perform(post("/api/tags")
                        .header("Authorization", "Bearer " + testUserAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                // [Then]
                .andExpect(status().isOk()) // [!!] 201(Created)이 아닌 200(OK)이 와야 함 (Find 성공)
                .andExpect(jsonPath("$.tagId").value(existingTag.getTagId())) // 기존 태그 ID와 동일해야 함
                .andExpect(jsonPath("$.name").value("사회"))
                .andDo(print());
    }

    @Test
    @DisplayName("[API 2] 신규 '전공-학과' 태그 생성 (POST /api/tags) 성공 (Create)")
    void createTag_New_MajorDept_Success() throws Exception {
        // [Given] "컴퓨터공학부" (전공-학과) 태그 DTO
        String jsonRequest = objectMapper.writeValueAsString(
                new TagCreateRequest(testDepartment.getName(), TagCategory.MAJOR_DEPT, testDepartment.getDepartment_id())
        );

        // [When]
        mockMvc.perform(post("/api/tags")
                        .header("Authorization", "Bearer " + testUserAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                // [Then]
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(jsonPath("$.name").value("컴퓨터공학부"))
                .andExpect(jsonPath("$.category").value("MAJOR_DEPT"))
                .andDo(print());
    }

    @Test
    @DisplayName("[API 2] '전공-과목' (자유입력) 태그 생성 (POST /api/tags) 성공 (Create)")
    void createTag_New_MajorCourse_Success() throws Exception {
        // [Given] "AI 프로그래밍" (전공-과목) 태그 DTO (사용자 흐름 5단계)
        String jsonRequest = objectMapper.writeValueAsString(
                new TagCreateRequest("AI 프로그래밍", TagCategory.MAJOR_COURSE, null)
        );

        // [When]
        mockMvc.perform(post("/api/tags")
                        .header("Authorization", "Bearer " + testUserAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                // [Then]
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(jsonPath("$.name").value("AI 프로그래밍"))
                .andExpect(jsonPath("$.category").value("MAJOR_COURSE"))
                .andDo(print());
    }
}
