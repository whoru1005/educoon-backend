package com.educoon.domain.tag;

import com.educoon.domain.department.Department;
import com.educoon.domain.department.DepartmentRepository;
import com.educoon.domain.department.DepartmentResponse;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * 사용자가 입력한 새 태그를 생성
     * @return
     */
    public TagResponse createTag(TagCreateRequest request){

        Department department = null;

//      전공-학과 태그일 경우, Department 엔티티 찾아서 연결
        if(request.getDepartmentId() != null){
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND));

            Optional<Tag> existingTag = tagRepository.findByNameAndCategoryAndDepartment(
                    request.getName(), request.getTagCategory(), department);

            if(existingTag.isPresent()){
                return new TagResponse(existingTag.get());
            }
        }else {
//            교외/전공과목/교양
            Optional<Tag> existingTag = tagRepository.findByNameAndCategory(
                    request.getName(), request.getTagCategory());

            if(existingTag.isPresent()){
                return new TagResponse(existingTag.get());
            }
        }

//        존재하지 않는 태그 확인하고 생성
        Tag newTag = request.toEntity(department);
        tagRepository.save(newTag);
        return new TagResponse(newTag);
    }

}
