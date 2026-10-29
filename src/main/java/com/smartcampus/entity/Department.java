package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity @Table(name = "departments") @Data
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer departmentId;

    @Column(name = "department_name", nullable = false, unique = true)
    private String departmentName;
}
