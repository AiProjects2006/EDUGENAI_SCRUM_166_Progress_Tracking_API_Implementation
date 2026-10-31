package com.ailearning.progressservice.repository;

import com.ailearning.progressservice.entity.StudentBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentBadgeRepository extends JpaRepository<StudentBadge, Long> {
    List<StudentBadge> findByProgressSummary_UserId(Long userId);
}