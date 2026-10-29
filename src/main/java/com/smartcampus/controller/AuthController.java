package com.smartcampus.controller;

import com.smartcampus.dto.AuthDtos.*;
import com.smartcampus.entity.User;
import com.smartcampus.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest req) {
        AuthService.requireStudentRegistrationRole(req.role());
        User user = authService.register(req.name(), req.email(), req.password(), req.departmentId());
        return new UserResponse(user.getUserId(), user.getName(), user.getEmail(), user.getRole().name(),
                user.getDepartment() == null ? null : user.getDepartment().getDepartmentId());
    }

    // POST /api/auth/login — matches slide 7's API contract
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        AuthService.AuthResult result = authService.login(req.email(), req.password());
        User user = result.user();
        return new AuthResponse(
                result.token(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getDepartment() == null ? null : user.getDepartment().getDepartmentId(),
                user.getDepartment() == null ? null : user.getDepartment().getDepartmentName());
    }
}
