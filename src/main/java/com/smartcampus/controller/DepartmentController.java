package com.smartcampus.controller;

import com.smartcampus.dto.DepartmentDtos.DepartmentResponse;
import com.smartcampus.repository.DepartmentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public List<DepartmentResponse> list() {
        return departmentRepository.findAllByOrderByDepartmentIdAsc().stream()
                .map(department -> new DepartmentResponse(
                        department.getDepartmentId(), department.getDepartmentName()))
                .toList();
    }
}
