package com.educoon.domain.department;

import lombok.Getter;

@Getter
public class DepartmentResponse {
    private Long departmentId;
    private String name;

    public DepartmentResponse(Long departmentId, String name) {
        this.departmentId = departmentId;
        this.name = name;
    }
}
