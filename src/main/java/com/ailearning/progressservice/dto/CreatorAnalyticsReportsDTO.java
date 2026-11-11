package com.ailearning.progressservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatorAnalyticsReportsDTO {

    // 1. Top 4 Overview Metric Cards
    private BigDecimal averageGrade;              // e.g. 84.2%
    private BigDecimal completionRate;            // e.g. 76.8%
    private Long badgesIssued;                    // e.g. 1204
    private Long activeLearningMinutes;           // e.g. 42500 MIN

    // 2. Subject Performance Comparison (Mastered vs Target)
    private List<SubjectPerformanceDTO> subjectPerformance;

    // 3. Grade Distribution (Bell Curve)
    private GradeDistributionDTO gradeDistribution;

    // 4. Curriculum Effectiveness (Module Health Table)
    private List<CurriculumModuleEffectivenessDTO> curriculumEffectiveness;

    // 5. Active Learning Minutes by Weekday (Daily Bar Chart)
    private List<DailyLearningMinutesDTO> activeLearningMinutesByDay;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubjectPerformanceDTO {
        private String subject;                   // "Environmental Studies", "Mathematics", "ICT"
        private BigDecimal masteredPercentage;    // e.g. 82.0%
        private BigDecimal targetPercentage;      // e.g. 90.0%
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradeDistributionDTO {
        private BigDecimal medianScore;           // e.g. 84.0%
        private List<ScoreBucketDTO> buckets;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreBucketDTO {
        private String grade;                     // "F", "D", "C", "B", "A"
        private String range;                     // "0-59%", "60-69%", "70-79%", "80-89%", "90-100%"
        private Long count;                       // number of submissions in bucket
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurriculumModuleEffectivenessDTO {
        private Long moduleId;
        private String moduleName;                // e.g. "E1.2: Ecosystems in Balance"
        private Integer grade;                    // e.g. 4, 7, 9
        private BigDecimal avgScore;              // e.g. 92.0%
        private String action;                    // "ON_TRACK", "MONITOR", "NEEDS_REVISION"
        private String actionLabel;               // "On Track", "Monitor", "Review Content"
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyLearningMinutesDTO {
        private String day;                       // "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"
        private Long minutes;                     // Total minutes spent
    }
}

