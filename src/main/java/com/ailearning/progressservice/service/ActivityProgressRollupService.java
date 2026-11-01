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

        // 1. Dynamic total levels (defaults to 6 if not passed)
        int totalLevels = (req.getTotalLevels() != null && req.getTotalLevels() > 0)
                ? req.getTotalLevels()
                : (mp.getTotalLevels() != null ? mp.getTotalLevels() : 6);
        mp.setTotalLevels(totalLevels);

        int levelNum = req.getLevelNumber() != null ? req.getLevelNumber() : 1;
        boolean isBoss = Boolean.TRUE.equals(req.getIsBossLevel()) || levelNum >= totalLevels;

        if (isBoss) {
            // Boss / Final level: Cap at totalLevels (never advance past totalLevels!)
            mp.setCurrentLevelId(Math.min(levelNum, totalLevels));
            if (!Boolean.TRUE.equals(mp.getIsCompleted())) {
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
                }
            }
        } else {
            // Non-boss level: Advance to next level, but never exceed totalLevels
            int nextLevel = Math.min(levelNum + 1, totalLevels);
            if (nextLevel > mp.getCurrentLevelId()) {
                mp.setCurrentLevelId(nextLevel);
            }
            int spent = req.getTimeSpentMins() != null ? req.getTimeSpentMins() : 3;
            mp.setTimeSpentMins(mp.getTimeSpentMins() + spent);
        }
        moduleRepo.save(mp);

        // 2. Curriculum Quest Progress Rollup
        ProgressSummary ps = summaryRepo.findById(req.getUserId()).orElseGet(() ->
                summaryRepo.save(ProgressSummary.builder()
                        .userId(req.getUserId())
                        .activitiesDone(0)
                        .starsEarned(0)
                        .mapActivitiesDone(0)
                        .practiceActivitiesDone(0)
                        .questStarsEarned(0)
                        .practiceStarsEarned(0)
                        .modulesCompleted(0)
                        .coursesCompleted(0)
                        .build()));

        int stars = isBoss ? 15 : 5;
        ps.recordMapActivity(stars);

        if (isBoss) {
            ps.setModulesCompleted(ps.getModulesCompleted() + 1);
            if (mp.getCourseProgress() != null && "COMPLETED".equals(mp.getCourseProgress().getStatus())) {
                ps.setCoursesCompleted(ps.getCoursesCompleted() + 1);
            }
        }

        // Check badge unlocks
        if (ps.getStarsEarned() >= 30) {
            unlockBadge(ps.getUserId(), "math");
        }
        if (ps.getStarsEarned() >= 60) {
            unlockBadge(ps.getUserId(), "ict");
        }
        if (ps.getStarsEarned() >= 100) {
            unlockBadge(ps.getUserId(), "environmental");
        }
        summaryRepo.save(ps);

        // 3. Save Record in ActivityProgressRecord
        BigDecimal score = req.getScorePercentage() != null ? req.getScorePercentage() : BigDecimal.valueOf(100);
        String subjectName = (mp.getCourseProgress() != null && mp.getCourseProgress().getSubjectProgress() != null)
                ? mp.getCourseProgress().getSubjectProgress().getSubjectName()
                : "Mathematics";

        recordRepo.save(ActivityProgressRecord.builder()
                .moduleProgress(mp)
                .userId(req.getUserId())
                .activityId(req.getModuleId() * 100 + levelNum)
                .levelNumber(levelNum)
                .activityType("CORE_MAP")
                .subjectName(subjectName)
                .scorePercentage(score)
                .marksObtained(score)
                .totalMarks(BigDecimal.valueOf(100))
                .timeSpentMins(req.getTimeSpentMins() != null ? req.getTimeSpentMins() : 3)
                .isPassed(score.compareTo(BigDecimal.valueOf(60)) >= 0)
                .build());

        // 4. Update Aggregate Topic Analytics
        updateTopicAnalytics(req.getModuleId(), req.getCourseId(), subjectName, score, true, isBoss);
    }

    @Transactional
    public void recordPracticeActivity(PracticeActivityRequestDTO req) {
        ProgressSummary ps = summaryRepo.findById(req.getUserId()).orElseGet(() ->
                summaryRepo.save(ProgressSummary.builder()
                        .userId(req.getUserId())
                        .activitiesDone(0)
                        .starsEarned(0)
                        .mapActivitiesDone(0)
                        .practiceActivitiesDone(0)
                        .questStarsEarned(0)
                        .practiceStarsEarned(0)
                        .modulesCompleted(0)
                        .coursesCompleted(0)
                        .build()));

        int stars = req.getStarsEarned() != null ? req.getStarsEarned() : 10;
        ps.recordPracticeActivity(stars);

        // Check badge unlocks
        if (ps.getStarsEarned() >= 30) {
            unlockBadge(ps.getUserId(), "math");
        }
        if (ps.getStarsEarned() >= 60) {
            unlockBadge(ps.getUserId(), "ict");
        }
        if (ps.getStarsEarned() >= 100) {
            unlockBadge(ps.getUserId(), "environmental");
        }
        summaryRepo.save(ps);

        String subjectName = req.getSubjectName() != null ? req.getSubjectName() : "Mathematics";

        // Boost subject progress by +2% (practice mastery bonus, does NOT complete course)
        subjectRepo.findByProgressSummary_UserIdAndSubjectNameIgnoreCase(req.getUserId(), subjectName)
                .ifPresent(sp -> {
                    BigDecimal newPct = sp.getCompletionPercentage().add(BigDecimal.valueOf(2)).min(BigDecimal.valueOf(100));
                    sp.setCompletionPercentage(newPct);
                    sp.setStarsEarned(sp.getStarsEarned() + stars);
                    subjectRepo.save(sp);
                });

        // Save Record in ActivityProgressRecord
        BigDecimal score = req.getScorePercentage() != null ? req.getScorePercentage() : BigDecimal.valueOf(80);
        recordRepo.save(ActivityProgressRecord.builder()
                .userId(req.getUserId())
                .activityId(req.getActivityId())
                .activityType("AI_PRACTICE")
                .subjectName(subjectName)
                .scorePercentage(score)
                .marksObtained(score)
                .totalMarks(BigDecimal.valueOf(100))
                .timeSpentMins(req.getTimeSpentMins() != null ? req.getTimeSpentMins() : 5)
                .isPassed(score.compareTo(BigDecimal.valueOf(60)) >= 0)
                .build());
    }

    private void unlockBadge(Long userId, String badgeCode) {
        badgeRepo.findByBadgeCodeAndProgressSummary_UserId(badgeCode, userId)
                .ifPresent(b -> {
                    if (!Boolean.TRUE.equals(b.getIsEarned())) {
                        b.setIsEarned(true);
                        b.setEarnedAt(LocalDateTime.now());
                        badgeRepo.save(b);
                    }
                });
    }

    private void updateTopicAnalytics(Long moduleId, Long courseId, String subjectName, BigDecimal score, boolean isMap, boolean completed) {
        AggregateTopicAnalytics analytics = analyticsRepo.findByModuleId(moduleId).orElseGet(() ->
                AggregateTopicAnalytics.builder()
                        .moduleId(moduleId)
                        .courseId(courseId)
                        .subjectName(subjectName)
                        .totalAttempts(0)
                        .totalCompletions(0)
                        .mapActivitiesCount(0)
                        .aiGeneratedActivitiesCount(0)
                        .averageScore(BigDecimal.ZERO)
                        .build());

        int prevAttempts = analytics.getTotalAttempts() != null ? analytics.getTotalAttempts() : 0;
        int newAttempts = prevAttempts + 1;
        analytics.setTotalAttempts(newAttempts);

        if (isMap) {
            analytics.setMapActivitiesCount((analytics.getMapActivitiesCount() != null ? analytics.getMapActivitiesCount() : 0) + 1);
        } else {
            analytics.setAiGeneratedActivitiesCount((analytics.getAiGeneratedActivitiesCount() != null ? analytics.getAiGeneratedActivitiesCount() : 0) + 1);
        }

        if (completed) {
            analytics.setTotalCompletions((analytics.getTotalCompletions() != null ? analytics.getTotalCompletions() : 0) + 1);
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
