package com.smartcampus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Request/response shapes for POST /api/auth/login and /api/auth/register
public class AuthDtos {

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            String role,
            @Positive Integer departmentId) {}

    public record LoginRequest(@NotBlank @Email @Size(max = 150) String email, @NotBlank String password) {}

    public record StaffRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Pattern(regexp = "ADMIN|FACULTY") String role,
            Integer departmentId) {}

    public record AuthResponse(
            String token,
            String name,
            String email,
            String role,
            Integer departmentId,
            String department) {}

    public record UserResponse(Integer userId, String name, String email, String role, Integer departmentId) {}
}
