package com.educoon.domain.tag.service;

import com.educoon.domain.department.entity.Department;
import com.educoon.domain.department.repository.DepartmentRepository;
import com.educoon.domain.tag.repository.TagRepository;
import com.educoon.domain.tag.dto.TagCreateRequest;
import com.educoon.domain.tag.dto.TagResponse;
import com.educoon.domain.tag.entity.Tag;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * 사용자가 입력한 태그가 있는지 확인하고 없으면 생성
     * @return
     */
    public TagResponse createTag(TagCreateRequest request){

        Department department = null;

        if(request.getDepartmentId() != null){
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND));

            Optional<Tag> existingTag = tagRepository.findByNameAndCategoryAndDepartment(
                    request.getName(), request.getTagCategory(), department);

            if(existingTag.isPresent()){
                return new TagResponse(existingTag.get());
            }
        }else{
            Optional<Tag> existingTag = tagRepository.findByNameAndCategory(
                    request.getName(), request.getTagCategory()
            );

            if(existingTag.isPresent()){
                return new TagResponse(existingTag.get());
            }
        }

        Tag newTag = request.toEntity(department);
        tagRepository.save(newTag);
        return new TagResponse(newTag);
    }

}
