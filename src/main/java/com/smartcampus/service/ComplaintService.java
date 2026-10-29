package com.smartcampus.service;

import com.smartcampus.entity.*;
import com.smartcampus.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Business layer: create complaints, apply status transitions, and keep the
// status_history trail in sync with every change (slide 6: Business Layer).
@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepo;
    private final StatusHistoryRepository historyRepo;
    private final DepartmentRepository departmentRepo;
    private final NotificationService notificationService;

    public ComplaintService(ComplaintRepository complaintRepo, StatusHistoryRepository historyRepo,
                             DepartmentRepository departmentRepo, NotificationService notificationService) {
        this.complaintRepo = complaintRepo;
        this.historyRepo = historyRepo;
        this.departmentRepo = departmentRepo;
        this.notificationService = notificationService;
    }

    @Transactional
    public Complaint createComplaint(User student, String category, Integer departmentId, String description) {
        Department dept = departmentRepo.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown department"));
        Complaint c = new Complaint();
        c.setUser(student);
        c.setCategory(category);
        c.setDepartment(dept);
        c.setDescription(description);
        c.setStatus(ComplaintStatus.OPEN);
        c = complaintRepo.save(c);
        recordHistory(c, ComplaintStatus.OPEN);
        return c;
    }

    @Transactional
    public Complaint updateStatus(Integer complaintId, ComplaintStatus newStatus) {
        Complaint c = complaintRepo.findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));
        c.setStatus(newStatus);
        complaintRepo.save(c);
        recordHistory(c, newStatus);
        notificationService.notifyStatusChange(c);
        return c;
    }

    private void recordHistory(Complaint c, ComplaintStatus status) {
        StatusHistory h = new StatusHistory();
        h.setComplaint(c);
        h.setStatus(status);
        historyRepo.save(h);
    }

    public List<Complaint> forStudent(User student) {
        return complaintRepo.findByUser(student);
    }

    public List<Complaint> forStudent(String email) {
        return complaintRepo.findByUser_EmailOrderByCreatedAtDesc(email);
    }

    public List<Complaint> forDepartment(Integer departmentId) {
        return complaintRepo.findByDepartment_DepartmentId(departmentId);
    }

    public List<Complaint> forDepartmentRecent(Integer departmentId) {
        return complaintRepo.findByDepartment_DepartmentIdOrderByCreatedAtDesc(departmentId);
    }

    public List<Complaint> all() {
        return complaintRepo.findAll();
    }

    public Complaint findById(Integer complaintId) {
        return complaintRepo.findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));
    }

    public Complaint findByIdForStudent(Integer complaintId, String email) {
        return complaintRepo.findByComplaintIdAndUser_Email(complaintId, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));
    }

    // Analytics module: pending workload per department (slide 5 & 7).
    public Map<String, Long> pendingByDepartment() {
        return complaintRepo.findAll().stream()
                .filter(c -> c.getStatus() != ComplaintStatus.RESOLVED)
                .collect(Collectors.groupingBy(c -> c.getDepartment().getDepartmentName(), Collectors.counting()));
    }
}
