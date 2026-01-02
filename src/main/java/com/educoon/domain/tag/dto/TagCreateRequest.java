package com.educoon.domain.tag.dto;

import com.educoon.domain.department.entity.Department;
import com.educoon.domain.tag.entity.Tag;
import com.educoon.domain.tag.entity.TagCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TagCreateRequest {

    @NotBlank(message = "태그 이름은 필수입니다")
    private String name;

    private TagCategory tagCategory;

//    전공 태그 일때만 값이 넘어옴
    private Long departmentId;

    public Tag toEntity(Department department){
        return Tag.builder()
                .name(this.name)
                .category(this.tagCategory)
                .department(department)
                .build();

    }

}
