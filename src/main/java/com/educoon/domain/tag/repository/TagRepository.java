package com.educoon.domain.tag.repository;

import com.educoon.domain.department.entity.Department;
import com.educoon.domain.tag.entity.Tag;
import com.educoon.domain.tag.entity.TagCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameAndCategory(String name, TagCategory category);

    /**
     * 태그 중복을 검사 위한 메소드
     * (이름 + 카테고리 + 학과) 모두 동일한 태그 찾음
     */
    Optional<Tag> findByNameAndCategoryAndDepartment(String name, TagCategory category, Department department);
}
