
package com.smartcampus.controller;

import com.smartcampus.dto.ComplaintDtos.*;
import com.smartcampus.entity.*;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.repository.StatusHistoryRepository;
import com.smartcampus.service.ComplaintService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final UserRepository userRepo;
    private final StatusHistoryRepository historyRepo;

    public ComplaintController(
            ComplaintService complaintService,
            UserRepository userRepo,
            StatusHistoryRepository historyRepo) {
        this.complaintService = complaintService;
        this.userRepo = userRepo;
        this.historyRepo = historyRepo;
    }

    // Student: view own complaints
    @GetMapping("/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ComplaintResponse> mine(Authentication auth) {
        return complaintService.forStudent(auth.getName())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Student: create complaint
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ComplaintResponse create(
            @Valid @RequestBody CreateComplaintRequest req,
            Authentication auth) {

        User student = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User account no longer exists"));

        Complaint complaint = complaintService.createComplaint(
                student,
                req.category(),
                req.departmentId(),
                req.description());

        return toResponse(complaint);
    }

    // Student, Admin or Faculty: view a complaint
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'FACULTY')")
    public ComplaintResponse get(
            @PathVariable Integer id,
            Authentication auth) {

        boolean isStudent = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

        Complaint complaint = isStudent
                ? complaintService.findByIdForStudent(id, auth.getName())
                : complaintService.findById(id);

        return toResponse(complaint);
    }

    // Admin or Faculty: list complaints
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public List<ComplaintResponse> list(Authentication auth) {

        boolean isFaculty = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));

        if (isFaculty) {
            User faculty = userRepo.findByEmail(auth.getName())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "User account no longer exists"));

            if (faculty.getDepartment() == null) {
                return List.of();
            }

            return complaintService
                    .forDepartmentRecent(
                            faculty.getDepartment().getDepartmentId())
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return complaintService.all()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Admin only: update complaint status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ComplaintResponse updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateStatusRequest req) {

        final ComplaintStatus status;

        try {
            String normalized = req.status().trim().replace(" ", "_").replace("-", "_").toUpperCase(Locale.ROOT);
            status = ComplaintStatus.valueOf(normalized);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unknown complaint status: " + req.status());
        }

        return toResponse(complaintService.updateStatus(id, status));
    }

    // Convert entity to response DTO
    private ComplaintResponse toResponse(Complaint complaint) {
        return new ComplaintResponse(
                complaint.getComplaintId(),
                complaint.getCategory(),
                complaint.getDepartment().getDepartmentId(),
                complaint.getDepartment().getDepartmentName(),
                complaint.getUser().getName(),
                complaint.getDescription(),
                complaint.getStatus().name(),
                complaint.getCreatedAt(),
                historyRepo
                        .findByComplaint_ComplaintIdOrderByUpdatedAtAsc(
                                complaint.getComplaintId())
                        .stream()
                        .map(item -> new HistoryResponse(
                                item.getStatus().name(),
                                item.getUpdatedAt()))
                        .toList());
    }
}
