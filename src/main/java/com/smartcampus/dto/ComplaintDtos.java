package com.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

// Request/response shapes for the complaint endpoints
public class ComplaintDtos {

    public record CreateComplaintRequest(
            @NotBlank @Size(max = 50) String category,
            @NotNull @Positive Integer departmentId,
            @NotBlank @Size(max = 4000) String description) {}

    public record UpdateStatusRequest(@NotBlank String status) {}

    public record ComplaintResponse(
            Integer complaintId,
            String category,
            Integer departmentId,
            String departmentName,
            String studentName,
            String description,
            String status,
            LocalDateTime createdAt,
            List<HistoryResponse> history) {}

    public record HistoryResponse(String status, LocalDateTime updatedAt) {}
}
