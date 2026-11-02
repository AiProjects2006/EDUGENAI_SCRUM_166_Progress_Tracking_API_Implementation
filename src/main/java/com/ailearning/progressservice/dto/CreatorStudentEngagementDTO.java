package com.ailearning.progressservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatorStudentEngagementDTO {

    // 1. Top KPI Summary Cards
    private BigDecimal weeklyEngagementHours;     // e.g. 4.2 hours
    private BigDecimal completionRate;            // e.g. 88.0%
    private Long activeStudentsCount;             // e.g. 1240
    private BigDecimal engagementGrowthPercentage;// e.g. +12.0%

    // 2. Daily Active Engagement (Subject-wise: Environmental Studies, Mathematics, ICT)
    private List<DailyEngagementDTO> dailyActiveEngagement;

    // 3. Time Spent per Subject (Donut Chart: Environmental Studies, Mathematics, ICT)
    private List<SubjectTimeDistributionDTO> timeSpentPerSubject;

    // 4. Most Engaging Lessons (Horizontal Bar Chart)
    private List<EngagingLessonDTO> mostEngagingLessons;

    // 5. High Engagement Students (Leaderboard Table)
    private List<HighEngagementStudentDTO> highEngagementStudents;

    // 6. Retention Risk Alerts (3+ days idle)
    private List<RetentionRiskDTO> retentionRisks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyEngagementDTO {
        private String date;                      // e.g. "Oct 01" or "2026-10-01"
        private Long environmentalCount;          // Environmental Studies interactions
        private Long mathCount;                   // Mathematics interactions
        private Long ictCount;                    // ICT interactions
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubjectTimeDistributionDTO {
        private String subject;                   // "Environmental Studies", "Mathematics", "ICT"
        private BigDecimal percentage;            // e.g. 45.0%
        private BigDecimal avgHoursPerStudent;    // e.g. 7.2 hrs/student
        private Long totalMinutesSpent;           // Total minutes
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EngagingLessonDTO {
        private String lessonTitle;               // e.g. "Ecosystems in Nature"
        private String subject;                   // "Environmental Studies", "Mathematics", "ICT"
        private Integer grade;                    // e.g. 6, 7, 8, 9
        private BigDecimal engagementRate;        // e.g. 94.0%
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HighEngagementStudentDTO {
        private Long studentId;
        private String name;
        private Integer grade;
        private BigDecimal avgWeeklyHours;        // e.g. 12.4
        private BigDecimal completionPercentage;  // e.g. 98.0
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RetentionRiskDTO {
        private Long studentId;
        private String name;
        private String subject;                   // "Environmental Studies", "Mathematics", "ICT"
        private Integer grade;
        private Long daysIdle;                    // e.g. 5, 4, 3
        private Boolean isAtRisk;
    }
}
