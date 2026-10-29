package com.smartcampus.service;

import com.smartcampus.entity.*;
import com.smartcampus.repository.*;
import com.smartcampus.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final DepartmentRepository departmentRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepo, DepartmentRepository departmentRepo,
                        PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.departmentRepo = departmentRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public User register(String name, String email, String rawPassword, Integer departmentId) {
        return saveUser(name, email, rawPassword, Role.STUDENT, departmentId);
    }

    public User registerStaff(String name, String email, String rawPassword, Role role, Integer departmentId) {
        if (role != Role.ADMIN && role != Role.FACULTY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff role must be ADMIN or FACULTY");
        }
        if (role == Role.FACULTY && departmentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculty accounts require a department");
        }
        return saveUser(name, email, rawPassword, role, departmentId);
    }

    private User saveUser(String name, String email, String rawPassword, Role role, Integer departmentId) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (userRepo.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        User u = new User();
        u.setName(name.trim());
        u.setEmail(normalizedEmail);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        if (departmentId != null) {
            u.setDepartment(departmentRepo.findById(departmentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown department")));
        }
        return userRepo.save(u);
    }

    public AuthResult login(String email, String rawPassword) {
        User u = userRepo.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(rawPassword, u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new AuthResult(jwtUtil.generateToken(u.getEmail(), u.getRole().name()), u);
    }

    public static void requireStudentRegistrationRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return;
        }
        final Role role;
        try {
            role = Role.valueOf(requestedRole.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role");
        }
        if (role != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Public registration is limited to students");
        }
    }

    public record AuthResult(String token, User user) {}
}
