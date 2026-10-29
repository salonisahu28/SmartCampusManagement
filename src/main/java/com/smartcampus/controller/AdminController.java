package com.smartcampus.controller;

import com.smartcampus.dto.AuthDtos.StaffRequest;
import com.smartcampus.dto.AuthDtos.UserResponse;
import com.smartcampus.entity.Role;
import com.smartcampus.entity.User;
import com.smartcampus.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public UserResponse createStaff(@Valid @RequestBody StaffRequest request) {
        User user = authService.registerStaff(
                request.name(),
                request.email(),
                request.password(),
                Role.valueOf(request.role()),
                request.departmentId());
        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getDepartment() == null ? null : user.getDepartment().getDepartmentId());
    }
}
