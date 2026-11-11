package com.ailearning.progressservice.repository;

import com.ailearning.progressservice.entity.ModuleProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ModuleProgressRepository extends JpaRepository<ModuleProgress, Long> {
    Optional<ModuleProgress> findByUserIdAndModuleId(Long userId, Long moduleId);

    List<ModuleProgress> findByCourseProgress_ProgressId(Long progressId);
}