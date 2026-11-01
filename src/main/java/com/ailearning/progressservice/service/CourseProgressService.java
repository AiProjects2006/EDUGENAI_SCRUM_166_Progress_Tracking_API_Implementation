package com.ailearning.progressservice.service;

import com.ailearning.progressservice.dto.CourseProgressResponseDTO;
import com.ailearning.progressservice.dto.ModuleProgressResponseDTO;
import com.ailearning.progressservice.entity.*;
import com.ailearning.progressservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseProgressService {

    private final CourseProgressRepository courseRepo;
    private final ModuleProgressRepository moduleRepo;
    private final SubjectProgressRepository subjectRepo;
    private final ProgressSummaryService summaryService;

    @Transactional
    public CourseProgress getOrCreateCourseProgress(Long userId, Long subjectId, Long courseId, int totalModules) {
        return courseRepo.findByUserIdAndCourseId(userId, courseId).orElseGet(() -> {
            ProgressSummary summary = summaryService.getOrCreateSummary(userId);
            SubjectProgress subject = subjectRepo.findByProgressSummary_UserId(userId).stream()
                    .filter(s -> s.getSubjectId().equals(subjectId))
                    .findFirst()
                    .orElseGet(() -> subjectRepo.findByProgressSummary_UserId(userId).get(0));

            CourseProgress cp = CourseProgress.builder()
                    .userId(userId)
                    .subjectProgress(subject)
                    .courseId(courseId)
                    .totalModules(totalModules > 0 ? totalModules : 4)
                    .completedModules(0)
                    .completionPercentage(BigDecimal.ZERO)
                    .status("IN_PROGRESS")
                    .build();
            return courseRepo.save(cp);
        });
    }

    @Transactional
    public ModuleProgress getOrCreateModuleProgress(Long userId, Long courseId, Long moduleId) {
        return moduleRepo.findByUserIdAndModuleId(userId, moduleId).orElseGet(() -> {
            CourseProgress cp = getOrCreateCourseProgress(userId, 1L, courseId, 4);
            ModuleProgress mp = ModuleProgress.builder()
                    .courseProgress(cp)
                    .moduleId(moduleId)
                    .userId(userId)
                    .currentLevelId(1)
                    .totalLevels(6)
                    .isCompleted(false)
                    .build();
            return moduleRepo.save(mp);
        });
    }

    @Transactional(readOnly = true)
    public CourseProgressResponseDTO getCourseProgress(Long userId, Long courseId) {
        Optional<CourseProgress> cp = courseRepo.findByUserIdAndCourseId(userId, courseId);
        if (cp.isEmpty()) {
            return CourseProgressResponseDTO.builder()
                    .courseId(courseId)
                    .completedModules(0)
                    .totalModules(4)
                    .completionPercentage(BigDecimal.ZERO)
                    .status("NOT_STARTED")
                    .build();
        }
        CourseProgress p = cp.get();
        return CourseProgressResponseDTO.builder()
                .courseId(p.getCourseId())
                .completedModules(p.getCompletedModules())
                .totalModules(p.getTotalModules())
                .completionPercentage(p.getCompletionPercentage())
                .status(p.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public ModuleProgressResponseDTO getModuleProgress(Long userId, Long moduleId) {
        Optional<ModuleProgress> mp = moduleRepo.findByUserIdAndModuleId(userId, moduleId);
        return mp.map(m -> {
            int total = m.getTotalLevels() != null ? m.getTotalLevels() : 6;
            int current = Math.min(m.getCurrentLevelId(), total);
            return ModuleProgressResponseDTO.builder()
                    .moduleId(m.getModuleId())
                    .currentLevelId(current)
                    .totalLevels(total)
                    .isCompleted(m.getIsCompleted())
                    .timeSpentMins(m.getTimeSpentMins())
                    .build();
        }).orElseGet(() -> ModuleProgressResponseDTO.builder()
                .moduleId(moduleId)
                .currentLevelId(1)
                .totalLevels(6)
                .isCompleted(false)
                .timeSpentMins(0)
                .build());
    }
}
