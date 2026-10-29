package com.smartcampus.dto;

public final class DepartmentDtos {

    private DepartmentDtos() {}

    public record DepartmentResponse(Integer departmentId, String departmentName) {}
}
