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

    public DepartmentResponse(Department department) {
        this.departmentId = department.getDepartment_id(); // (department의 getter 이름이 getId()라고 가정)
        this.name = department.getName();       // (department의 getter 이름이 getName()이라고 가정)
    }
}
