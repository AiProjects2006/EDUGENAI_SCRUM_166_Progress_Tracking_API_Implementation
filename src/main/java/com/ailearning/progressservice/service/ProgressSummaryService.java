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
        return summaryRepo.findById(userId).orElseGet(() -> {
            ProgressSummary summary = ProgressSummary.builder()
                    .userId(userId)
                    .coursesCompleted(0)
                    .modulesCompleted(0)
                    .activitiesDone(0)
                    .starsEarned(0)
                    .currentStreakDays(1)
                    .build();
            summary = summaryRepo.save(summary);

            // Seed default subjects matching Student UI
            createDefaultSubject(summary, 1L, "Mathematics", "#ef476f");
            createDefaultSubject(summary, 2L, "Science", "#06d6a0");
            createDefaultSubject(summary, 3L, "English", "#ffd166");
            createDefaultSubject(summary, 4L, "ICT", "#66f2ff");

            // Seed default badges
            createDefaultBadge(summary, "math", "Math Whiz", "twemoji:1st-place-medal");
            createDefaultBadge(summary, "science", "Science Pro", "twemoji:2nd-place-medal");
            createDefaultBadge(summary, "reader", "Reader", "twemoji:3rd-place-medal");
            createDefaultBadge(summary, "grammar", "Grammar Master", "twemoji:locked");

            return summary;
        });
    }

    private void createDefaultSubject(ProgressSummary summary, Long subjectId, String name, String color) {
        SubjectProgress sp = SubjectProgress.builder()
                .progressSummary(summary)
                .subjectId(subjectId)
                .subjectName(name)
                .colorCode(color)
                .completionPercentage(BigDecimal.ZERO)
                .build();
        subjectRepo.save(sp);
    }

    private void createDefaultBadge(ProgressSummary summary, String code, String name, String icon) {
        StudentBadge b = StudentBadge.builder()
                .progressSummary(summary)
                .badgeCode(code)
                .badgeName(name)
                .iconUrl(icon)
                .isEarned(false)
                .build();
        badgeRepo.save(b);
    }

    @Transactional(readOnly = true)
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

        return ProgressSummaryResponseDTO.builder()
                .userId(summary.getUserId())
                .coursesCompleted(summary.getCoursesCompleted())
                .modulesCompleted(summary.getModulesCompleted())
                .activitiesDone(summary.getActivitiesDone())
                .starsEarned(summary.getStarsEarned())
                .currentStreakDays(summary.getCurrentStreakDays())
                .averageScore(summary.getAverageScore())
                .subjectProgress(subjectMap)
                .badges(badgeList)
                .build();
    }
}