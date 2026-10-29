package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity @Table(name = "complaints") @Data
public class Complaint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer complaintId;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

    private String category;

    @ManyToOne @JoinColumn(name = "department_id")
    private Department department;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status = ComplaintStatus.OPEN;

    private LocalDateTime createdAt = LocalDateTime.now();
}
