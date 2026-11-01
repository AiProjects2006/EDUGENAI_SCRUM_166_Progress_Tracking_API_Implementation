package com.ailearning.progressservice.repository;

import com.ailearning.progressservice.entity.ActivityProgressRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityProgressRecordRepository extends JpaRepository<ActivityProgressRecord, Long> {
    List<ActivityProgressRecord> findByUserId(Long userId);
    List<ActivityProgressRecord> findByActivityType(String activityType);
    long countByActivityType(String activityType);
}
