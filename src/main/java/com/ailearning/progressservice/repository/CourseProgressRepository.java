package com.ailearning.progressservice.repository;

import com.ailearning.progressservice.entity.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {
    List<CourseProgress> findByUserId(Long userId);

    Optional<CourseProgress> findByUserIdAndCourseId(Long userId, Long courseId);
}