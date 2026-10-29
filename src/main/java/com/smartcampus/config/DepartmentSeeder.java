package com.smartcampus.config;

import com.smartcampus.entity.Department;
import com.smartcampus.repository.DepartmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DepartmentSeeder implements CommandLineRunner {

    private static final List<String> DEFAULT_DEPARTMENTS = List.of(
            "Electrical",
            "Housekeeping",
            "Civil/Infrastructure",
            "IT Services",
            "Hostel");

    private final DepartmentRepository departmentRepository;

    public DepartmentSeeder(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(String... args) {
        for (String name : DEFAULT_DEPARTMENTS) {
            if (departmentRepository.findByDepartmentNameIgnoreCase(name).isEmpty()) {
                Department department = new Department();
                department.setDepartmentName(name);
                departmentRepository.save(department);
            }
        }
    }
}
