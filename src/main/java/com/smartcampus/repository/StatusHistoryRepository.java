package com.smartcampus.repository;

import com.smartcampus.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Integer> {
    List<StatusHistory> findByComplaint_ComplaintIdOrderByUpdatedAtAsc(Integer complaintId);
}
