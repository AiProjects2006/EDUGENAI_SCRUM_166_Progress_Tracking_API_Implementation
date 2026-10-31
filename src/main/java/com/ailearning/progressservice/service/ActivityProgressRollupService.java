package com.ailearning.progressservice.service;

import com.ailearning.progressservice.dto.LevelCompleteRequestDTO;
import com.ailearning.progressservice.dto.PracticeActivityRequestDTO;
import com.ailearning.progressservice.entity.*;
import com.ailearning.progressservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityProgressRollupService {

    private final CourseProgressService courseProgressService;
    private final ModuleProgressRepository moduleRepo;
    private final CourseProgressRepository courseRepo;
    private final SubjectProgressRepository subjectRepo;
    private final ProgressSummaryRepository summaryRepo;
    private final ActivityProgressRecordRepository recordRepo;
    private final StudentBadgeRepository badgeRepo;
    private final AggregateTopicAnalyticsRepository analyticsRepo;

    @Transactional
    public void recordQuestLevelComplete(LevelCompleteRequestDTO req) {
        ModuleProgress mp = courseProgressService.getOrCreateModuleProgress(req.getUserId(), req.getCourseId(), req.getModuleId());

        // 1. Advance to next level on Island Map (1 to 6)
        if (req.getLevelNumber() != null && req.getLevelNumber() >= mp.getCurrentLevelId() && mp.getCurrentLevelId() < 6) {
            mp.setCurrentLevelId(req.getLevelNumber() + 1);
        }

        // 2. Beating Boss (Level 6) marks MODULE COMPLETE
        boolean isBoss = Boolean.TRUE.equals(req.getIsBossLevel()) || (req.getLevelNumber() != null && req.getLevelNumber() == 6);
        if (isBoss && !Boolean.TRUE.equals(mp.getIsCompleted())) {
            mp.setIsCompleted(true);
            mp.setCompletedAt(LocalDateTime.now());
            int spent = req.getTimeSpentMins() != null ? req.getTimeSpentMins() : 5;
            mp.setTimeSpentMins(mp.getTimeSpentMins() + spent);

            // Rollup to Course Progress
            CourseProgress cp = mp.getCourseProgress();
            if (cp != null) {
                cp.setCompletedModules(cp.getCompletedModules() + 1);
                if (cp.getTotalModules() > 0) {
                    BigDecimal pct = BigDecimal.valueOf(cp.getCompletedModules())
                            .divide(BigDecimal.valueOf(cp.getTotalModules()), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    cp.setCompletionPercentage(pct);
                    if (cp.getCompletedModules() >= cp.getTotalModules()) {
                        cp.setStatus("COMPLETED");
                    }
                }
                courseRepo.save(cp);

                // Rollup to Subject Progress
                SubjectProgress sp = cp.getSubjectProgress();
                if (sp != null && sp.getTotalCourses() > 0) {
                    if ("COMPLETED".equals(cp.getStatus())) {
                        sp.setCompletedCourses(sp.getCompletedCourses() + 1);
                    }
                    BigDecimal subPct = BigDecimal.valueOf(sp.getCompletedCourses())
                            .divide(BigDecimal.valueOf(sp.getTotalCourses()), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    sp.setCompletionPercentage(subPct);
                    subjectRepo.save(sp);
                }

                // Rollup to Progress Summary
                summaryRepo.findById(req.getUserId()).ifPresent(ps -> {
                    ps.setModulesCompleted(ps.getModulesCompleted() + 1);
                    if ("COMPLETED".equals(cp.getStatus())) {
                        ps.setCoursesCompleted(ps.getCoursesCompleted() + 1);
                    }
                    summaryRepo.save(ps);
                });
            }
        }
        moduleRepo.save(mp);

        // 3. Save Record in ActivityProgressRecord
        BigDecimal score = req.getScorePercentage() != null ? req.getScorePercentage() : BigDecimal.valueOf(100);
        recordRepo.save(ActivityProgressRecord.builder()
                .moduleProgress(mp)
                .userId(req.getUserId())
                .activityId(req.getModuleId() * 100 + (req.getLevelNumber() != null ? req.getLevelNumber() : 1))
                .levelNumber(req.getLevelNumber())
                .activityType("CORE_MAP")
                .scorePercentage(score)
                .marksObtained(score)
                .totalMarks(BigDecimal.valueOf(100))
                .timeSpentMins(req.getTimeSpentMins() != null ? req.getTimeSpentMins() : 3)
                .isPassed(score.compareTo(BigDecimal.valueOf(60)) >= 0)
                .build());

        // 4. Update Aggregate Topic Analytics for Backoffice UI
        updateTopicAnalytics(req.getModuleId(), req.getCourseId(), score, isBoss);
    }

    @Transactional
    public void recordPracticeActivity(PracticeActivityRequestDTO req) {
        ProgressSummary ps = summaryRepo.findById(req.getUserId()).orElseGet(() ->
                summaryRepo.save(ProgressSummary.builder().userId(req.getUserId()).build()));

        ps.setActivitiesDone(ps.getActivitiesDone() + 1);
        int stars = req.getStarsEarned() != null ? req.getStarsEarned() : 10;
        ps.setStarsEarned(ps.getStarsEarned() + stars);

        // Check badge unlocks based on stars earned
        if (ps.getStarsEarned() >= 100) {
            unlockBadge(ps.getUserId(), "math");
        }
        if (ps.getStarsEarned() >= 300) {
            unlockBadge(ps.getUserId(), "science");
        }
        summaryRepo.save(ps);

        // Boost subject progress by +5%
        if (req.getSubjectName() != null) {
            subjectRepo.findByProgressSummary_UserIdAndSubjectNameIgnoreCase(req.getUserId(), req.getSubjectName())
                    .ifPresent(sp -> {
                        BigDecimal newPct = sp.getCompletionPercentage().add(BigDecimal.valueOf(5)).min(BigDecimal.valueOf(100));
                        sp.setCompletionPercentage(newPct);
                        sp.setStarsEarned(sp.getStarsEarned() + stars);
                        subjectRepo.save(sp);
                    });
        }

        // Save Record in ActivityProgressRecord
        BigDecimal score = req.getScorePercentage() != null ? req.getScorePercentage() : BigDecimal.valueOf(80);
        recordRepo.save(ActivityProgressRecord.builder()
                .userId(req.getUserId())
                .activityId(req.getActivityId())
                .activityType("AI_PRACTICE")
                .scorePercentage(score)
                .marksObtained(score)
                .totalMarks(BigDecimal.valueOf(100))
                .timeSpentMins(req.getTimeSpentMins() != null ? req.getTimeSpentMins() : 5)
                .isPassed(score.compareTo(BigDecimal.valueOf(60)) >= 0)
                .build());
    }

    private void unlockBadge(Long userId, String badgeCode) {
        badgeRepo.findByProgressSummary_UserId(userId).stream()
                .filter(b -> b.getBadgeCode().equalsIgnoreCase(badgeCode) && !Boolean.TRUE.equals(b.getIsEarned()))
                .findFirst()
                .ifPresent(b -> {
                    b.setIsEarned(true);
                    b.setEarnedAt(LocalDateTime.now());
                    badgeRepo.save(b);
                });
    }

    private void updateTopicAnalytics(Long moduleId, Long courseId, BigDecimal score, boolean completed) {
        AggregateTopicAnalytics analytics = analyticsRepo.findByModuleId(moduleId).orElseGet(() ->
                AggregateTopicAnalytics.builder()
                        .moduleId(moduleId)
                        .courseId(courseId)
                        .totalAttempts(0)
                        .totalCompletions(0)
                        .averageScore(BigDecimal.ZERO)
                        .build());

        int prevAttempts = analytics.getTotalAttempts();
        int newAttempts = prevAttempts + 1;
        analytics.setTotalAttempts(newAttempts);

        if (completed) {
            analytics.setTotalCompletions(analytics.getTotalCompletions() + 1);
        }

        BigDecimal newAvg = analytics.getAverageScore().multiply(BigDecimal.valueOf(prevAttempts))
                .add(score)
                .divide(BigDecimal.valueOf(newAttempts), 2, RoundingMode.HALF_UP);
        analytics.setAverageScore(newAvg);

        if (newAvg.compareTo(BigDecimal.valueOf(50)) < 0) {
            analytics.setDifficultyRating("HARD");
        } else if (newAvg.compareTo(BigDecimal.valueOf(80)) > 0) {
            analytics.setDifficultyRating("EASY");
        } else {
            analytics.setDifficultyRating("MEDIUM");
        }

        analyticsRepo.save(analytics);
    }
}
