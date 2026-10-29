package com.smartcampus.repository;

import com.smartcampus.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Optional<Department> findByDepartmentNameIgnoreCase(String departmentName);
    java.util.List<Department> findAllByOrderByDepartmentIdAsc();
}
