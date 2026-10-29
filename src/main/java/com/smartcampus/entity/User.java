package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity @Table(name = "users") @Data
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String name;

    @Column(unique = true)
    private String email;

    @ToString.Exclude
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
}
