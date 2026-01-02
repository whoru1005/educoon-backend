package com.educoon.domain.department.service;

import com.educoon.domain.department.dto.DepartmentResponse;
import com.educoon.domain.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {


    private final DepartmentRepository departmentRepository;
    /**
     * 모든 학과학부 목록 조회
     */
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments(){
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::new)
                .collect(Collectors.toList());
    }

}
