package com.smartcampus.service;

import com.smartcampus.entity.Complaint;
import com.smartcampus.repository.ComplaintRepository;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.StatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ComplaintServiceTest {

    private final ComplaintRepository complaintRepository = mock(ComplaintRepository.class);
    private final ComplaintService complaintService = new ComplaintService(
            complaintRepository,
            mock(StatusHistoryRepository.class),
            mock(DepartmentRepository.class),
            mock(NotificationService.class));

    @Test
    void studentLookupIsScopedToAuthenticatedEmail() {
        Complaint complaint = new Complaint();
        when(complaintRepository.findByComplaintIdAndUser_Email(7, "student@example.com"))
                .thenReturn(Optional.of(complaint));

        assertSame(complaint, complaintService.findByIdForStudent(7, "student@example.com"));
        verify(complaintRepository).findByComplaintIdAndUser_Email(7, "student@example.com");
    }

    @Test
    void missingComplaintReturnsNotFound() {
        when(complaintRepository.findByComplaintIdAndUser_Email(7, "student@example.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> complaintService.findByIdForStudent(7, "student@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
