package com.ailearning.progressservice.service;

import com.ailearning.progressservice.dto.ProgressSummaryResponseDTO;
import com.ailearning.progressservice.entity.*;
import com.ailearning.progressservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProgressSummaryService {

    private final ProgressSummaryRepository summaryRepo;
    private final SubjectProgressRepository subjectRepo;
    private final StudentBadgeRepository badgeRepo;

    @Transactional
    public ProgressSummary getOrCreateSummary(Long userId) {
        ProgressSummary summary = summaryRepo.findById(userId).orElseGet(() -> {
            ProgressSummary newSummary = ProgressSummary.builder()
                    .userId(userId)
                    .coursesCompleted(0)
                    .modulesCompleted(0)
                    .activitiesDone(0)
                    .starsEarned(0)
                    .mapActivitiesDone(0)
                    .practiceActivitiesDone(0)
                    .questStarsEarned(0)
                    .practiceStarsEarned(0)
                    .currentStreakDays(1)
                    .build();
            return summaryRepo.save(newSummary);
        });

        // Ensure subjects and badges match the 3 core subjects: Mathematics, ICT, and Environmental Studies
        syncSubjectsAndBadges(summary);

        return summary;
    }

    private void syncSubjectsAndBadges(ProgressSummary summary) {
        Long userId = summary.getUserId();
        List<SubjectProgress> existingSubjects = subjectRepo.findByProgressSummary_UserId(userId);

        // Delete obsolete subjects (e.g. legacy Science, English)
        Set<String> validSubjectNames = Set.of("Mathematics", "ICT", "Environmental Studies");
        for (SubjectProgress sp : existingSubjects) {
            if (!validSubjectNames.contains(sp.getSubjectName())) {
                subjectRepo.delete(sp);
            }
        }

        // Ensure 3 subjects exist
        ensureSubject(summary, 1L, "Mathematics", "#ef476f");
        ensureSubject(summary, 2L, "ICT", "#66f2ff");
        ensureSubject(summary, 3L, "Environmental Studies", "#06d6a0");

        // Delete obsolete badges (e.g. legacy science, reader, grammar)
        List<StudentBadge> existingBadges = badgeRepo.findByProgressSummary_UserId(userId);
        Set<String> validBadgeCodes = Set.of("math", "ict", "environmental");
        for (StudentBadge b : existingBadges) {
            if (!validBadgeCodes.contains(b.getBadgeCode())) {
                badgeRepo.delete(b);
            }
        }

        // Ensure 3 badges matching the subjects exist
        ensureBadge(summary, "math", "Math Whiz", "twemoji:1st-place-medal");
        ensureBadge(summary, "ict", "ICT Wizard", "twemoji:laptop");
        ensureBadge(summary, "environmental", "Eco Champion", "twemoji:seedling");
    }

    private void ensureSubject(ProgressSummary summary, Long subjectId, String name, String color) {
        if (subjectRepo.findByProgressSummary_UserIdAndSubjectNameIgnoreCase(summary.getUserId(), name).isEmpty()) {
            SubjectProgress sp = SubjectProgress.builder()
                    .progressSummary(summary)
                    .subjectId(subjectId)
                    .subjectName(name)
                    .colorCode(color)
                    .completionPercentage(BigDecimal.ZERO)
                    .build();
            subjectRepo.save(sp);
        }
    }

    private void ensureBadge(ProgressSummary summary, String code, String name, String icon) {
        if (badgeRepo.findByBadgeCodeAndProgressSummary_UserId(code, summary.getUserId()).isEmpty()) {
            StudentBadge b = StudentBadge.builder()
                    .progressSummary(summary)
                    .badgeCode(code)
                    .badgeName(name)
                    .iconUrl(icon)
                    .isEarned(false)
                    .build();
            badgeRepo.save(b);
        }
    }

    @Transactional
    public ProgressSummaryResponseDTO getStudentProgressDashboard(Long userId) {
        ProgressSummary summary = getOrCreateSummary(userId);
        List<SubjectProgress> subjects = subjectRepo.findByProgressSummary_UserId(userId);
        List<StudentBadge> badges = badgeRepo.findByProgressSummary_UserId(userId);

        Map<String, ProgressSummaryResponseDTO.SubjectDetailDTO> subjectMap = new LinkedHashMap<>();
        for (SubjectProgress s : subjects) {
            subjectMap.put(s.getSubjectName(), new ProgressSummaryResponseDTO.SubjectDetailDTO(
                    s.getCompletionPercentage(), s.getColorCode()));
        }

        List<ProgressSummaryResponseDTO.BadgeDTO> badgeList = new ArrayList<>();
        for (StudentBadge b : badges) {
            badgeList.add(new ProgressSummaryResponseDTO.BadgeDTO(
                    b.getBadgeCode(), b.getBadgeName(), b.getIconUrl(), b.getIsEarned()));
        }

        int mapDone = summary.getMapActivitiesDone() != null ? summary.getMapActivitiesDone() : summary.getActivitiesDone();
        int practiceDone = summary.getPracticeActivitiesDone() != null ? summary.getPracticeActivitiesDone() : 0;
        int questStars = summary.getQuestStarsEarned() != null ? summary.getQuestStarsEarned() : summary.getStarsEarned();
        int practiceStars = summary.getPracticeStarsEarned() != null ? summary.getPracticeStarsEarned() : 0;

        return ProgressSummaryResponseDTO.builder()
                .userId(summary.getUserId())
                .coursesCompleted(summary.getCoursesCompleted())
                .modulesCompleted(summary.getModulesCompleted())
                .activitiesDone(mapDone + practiceDone) // Combined
                .starsEarned(questStars + practiceStars) // Combined
                .mapActivitiesDone(mapDone)
                .practiceActivitiesDone(practiceDone)
                .questStarsEarned(questStars)
                .practiceStarsEarned(practiceStars)
                .currentStreakDays(summary.getCurrentStreakDays())
                .averageScore(summary.getAverageScore())
                .subjectProgress(subjectMap)
                .badges(badgeList)
                .build();
    }
}
