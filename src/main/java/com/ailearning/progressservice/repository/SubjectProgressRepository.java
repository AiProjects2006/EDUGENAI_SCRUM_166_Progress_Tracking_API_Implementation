package com.ailearning.progressservice.repository;

import com.ailearning.progressservice.entity.SubjectProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubjectProgressRepository extends JpaRepository<SubjectProgress, Long> {
    List<SubjectProgress> findByProgressSummary_UserId(Long userId);

    Optional<SubjectProgress> findByProgressSummary_UserIdAndSubjectNameIgnoreCase(Long userId, String subjectName);
}