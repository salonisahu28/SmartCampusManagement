package com.smartcampus.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.smartcampus.entity.Role;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AuthServiceTest {

    @Test
    void publicRegistrationDefaultsToStudent() {
        assertDoesNotThrow(() -> AuthService.requireStudentRegistrationRole(null));
        assertDoesNotThrow(() -> AuthService.requireStudentRegistrationRole("student"));
    }

    @Test
    void publicRegistrationRejectsStaffRoles() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> AuthService.requireStudentRegistrationRole("ADMIN"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void publicRegistrationRejectsUnknownRoles() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> AuthService.requireStudentRegistrationRole("INVALID"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void staffRegistrationRequiresFacultyDepartment() {
        AuthService authService = new AuthService(
                mock(UserRepository.class),
                mock(DepartmentRepository.class),
                mock(PasswordEncoder.class),
                mock(JwtUtil.class));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.registerStaff("Faculty", "faculty@example.com", "password123", Role.FACULTY, null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void staffRegistrationCannotCreateStudents() {
        AuthService authService = new AuthService(
                mock(UserRepository.class),
                mock(DepartmentRepository.class),
                mock(PasswordEncoder.class),
                mock(JwtUtil.class));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.registerStaff("Student", "student@example.com", "password123", Role.STUDENT, null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
