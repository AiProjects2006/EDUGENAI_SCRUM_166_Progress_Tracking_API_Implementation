package com.ailearning.progressservice.service;

import com.ailearning.progressservice.dto.ActivityRecordResponseDTO;
import com.ailearning.progressservice.dto.CourseProgressResponseDTO;
import com.ailearning.progressservice.dto.ModuleLevelStatusDTO;
import com.ailearning.progressservice.dto.ModuleProgressResponseDTO;
import com.ailearning.progressservice.entity.*;
import com.ailearning.progressservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseProgressService {

    private final CourseProgressRepository courseRepo;
    private final ModuleProgressRepository moduleRepo;
    private final SubjectProgressRepository subjectRepo;
    private final ActivityProgressRecordRepository recordRepo;
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

    @Transactional(readOnly = true)
    public List<ModuleLevelStatusDTO> getModuleLevels(Long userId, Long moduleId) {
        Optional<ModuleProgress> mpOpt = moduleRepo.findByUserIdAndModuleId(userId, moduleId);
        int totalLevels = mpOpt.map(m -> m.getTotalLevels() != null ? m.getTotalLevels() : 6).orElse(6);
        int currentLevel = mpOpt.map(ModuleProgress::getCurrentLevelId).orElse(1);
        boolean isModuleDone = mpOpt.map(ModuleProgress::getIsCompleted).orElse(false);

        List<ActivityProgressRecord> records = recordRepo.findByUserIdAndModuleProgress_ModuleId(userId, moduleId);
        Map<Integer, ActivityProgressRecord> recordByLevel = records.stream()
                .filter(r -> r.getLevelNumber() != null)
                .collect(Collectors.toMap(
                        ActivityProgressRecord::getLevelNumber,
                        r -> r,
                        (existing, replacing) -> replacing
                ));

        List<ModuleLevelStatusDTO> result = new ArrayList<>();
        for (int lvl = 1; lvl <= totalLevels; lvl++) {
            ActivityProgressRecord r = recordByLevel.get(lvl);
            boolean isCompleted = isModuleDone || (lvl < currentLevel) || (r != null && Boolean.TRUE.equals(r.getIsPassed()));
            boolean isCurrent = !isModuleDone && (lvl == currentLevel);
            boolean isBoss = (lvl == totalLevels);

            String status = isCompleted ? "completed" : (isCurrent ? "current" : "locked");

            int stars = 0;
            BigDecimal score = r != null ? r.getScorePercentage() : null;
            Integer timeSpent = r != null ? r.getTimeSpentMins() : 0;

            if (isCompleted) {
                if (score != null) {
                    if (score.compareTo(BigDecimal.valueOf(80)) >= 0) stars = 3;
                    else if (score.compareTo(BigDecimal.valueOf(60)) >= 0) stars = 2;
                    else stars = 1;
                } else {
                    stars = 3;
                }
            }

            result.add(ModuleLevelStatusDTO.builder()
                    .levelNumber(lvl)
                    .status(status)
                    .isCompleted(isCompleted)
                    .isBoss(isBoss)
                    .stars(stars)
                    .scorePercentage(score)
                    .timeSpentMins(timeSpent)
                    .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ActivityRecordResponseDTO> getStudentActivityHistory(Long userId, String type, String subject, Integer limit) {
        List<ActivityProgressRecord> records;
        if (type != null && !type.isBlank()) {
            records = recordRepo.findByUserIdAndActivityTypeOrderByCompletedAtDesc(userId, type.toUpperCase());
        } else {
            records = recordRepo.findByUserIdOrderByCompletedAtDesc(userId);
        }

        return records.stream()
                .filter(r -> subject == null || subject.isBlank() || subject.equalsIgnoreCase(r.getSubjectName()))
                .limit(limit != null && limit > 0 ? limit : Long.MAX_VALUE)
                .map(r -> ActivityRecordResponseDTO.builder()
                        .recordId(r.getRecordId())
                        .activityId(r.getActivityId())
                        .activityType(r.getActivityType())
                        .levelNumber(r.getLevelNumber())
                        .subjectName(r.getSubjectName())
                        .moduleId(r.getModuleProgress() != null ? r.getModuleProgress().getModuleId() : null)
                        .scorePercentage(r.getScorePercentage())
                        .marksObtained(r.getMarksObtained())
                        .totalMarks(r.getTotalMarks())
                        .isPassed(r.getIsPassed())
                        .timeSpentMins(r.getTimeSpentMins())
                        .completedAt(r.getCompletedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
