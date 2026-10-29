package com.smartcampus.repository;

import com.smartcampus.entity.Complaint;
import com.smartcampus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {
    List<Complaint> findByUser(User user);
    List<Complaint> findByDepartment_DepartmentId(Integer departmentId);
    Optional<Complaint> findByComplaintIdAndUser_Email(Integer complaintId, String email);
    List<Complaint> findByUser_EmailOrderByCreatedAtDesc(String email);
    List<Complaint> findByDepartment_DepartmentIdOrderByCreatedAtDesc(Integer departmentId);
}
