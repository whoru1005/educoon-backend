package com.educoon.domain.tag;

import com.educoon.domain.department.DepartmentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    /**
     * 학교의 모든 학과학부 조회
     * [GET] /api/tags/departments
     */
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments(){
        List<DepartmentResponse> departments = tagService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @PostMapping
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagCreateRequest request){
        TagResponse newTag = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTag);
    }
}
