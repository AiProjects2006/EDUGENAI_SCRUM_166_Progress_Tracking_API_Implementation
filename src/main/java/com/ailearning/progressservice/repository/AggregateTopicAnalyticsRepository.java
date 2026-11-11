package com.ailearning.progressservice.repository;

import com.ailearning.progressservice.entity.AggregateTopicAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface AggregateTopicAnalyticsRepository extends JpaRepository<AggregateTopicAnalytics, Long> {
    Optional<AggregateTopicAnalytics> findByModuleId(Long moduleId);
    List<AggregateTopicAnalytics> findByCourseId(Long courseId);
}
