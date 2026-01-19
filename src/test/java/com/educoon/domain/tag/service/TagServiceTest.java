package com.educoon.domain.tag.service;

import com.educoon.domain.department.repository.DepartmentRepository;
import com.educoon.domain.tag.dto.TagCreateRequest;
import com.educoon.domain.tag.dto.TagResponse;
import com.educoon.domain.tag.entity.Tag;
import com.educoon.domain.tag.entity.TagCategory;
import com.educoon.domain.tag.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;
    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    @DisplayName("새로운 태그 생성 성공")
    void createTag_New_Success() {
        // given
        TagCreateRequest request = new TagCreateRequest("자바", TagCategory.MAJOR_COURSE, null);
        given(tagRepository.findByNameAndCategory(any(), any())).willReturn(Optional.empty());
        
        // when
        TagResponse result = tagService.createTag(request);

        // then
        assertThat(result.getName()).isEqualTo("자바");
        verify(tagRepository).save(any());
    }

    @Test
    @DisplayName("기존 태그 반환")
    void createTag_Existing_ReturnsExisting() {
        // given
        TagCreateRequest request = new TagCreateRequest("자바", TagCategory.MAJOR_COURSE, null);
        Tag existingTag = Tag.builder().name("자바").category(TagCategory.MAJOR_COURSE).build();
        given(tagRepository.findByNameAndCategory(any(), any())).willReturn(Optional.of(existingTag));

        // when
        TagResponse result = tagService.createTag(request);

        // then
        assertThat(result.getName()).isEqualTo("자바");
        // verify(tagRepository, never()).save(any()); // Mockito.never() 임포트 필요 시 사용
    }
}
