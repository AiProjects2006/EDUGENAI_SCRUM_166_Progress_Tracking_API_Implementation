package com.ailearning.progressservice.repository;

import com.ailearning.progressservice.entity.ProgressSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressSummaryRepository extends JpaRepository<ProgressSummary, Long> {
}