package com.educoon.domain.tag;

import com.educoon.domain.department.Department;
import com.educoon.domain.tagCategory.TagCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TagCreateRequest {

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
