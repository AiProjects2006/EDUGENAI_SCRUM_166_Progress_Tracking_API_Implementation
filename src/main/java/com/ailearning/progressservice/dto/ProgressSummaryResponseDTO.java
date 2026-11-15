package com.ailearning.progressservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressSummaryResponseDTO {
    private Long userId;

    // Combined & High-Level (Matches UI Progress.jsx)
    private Integer coursesCompleted;
    private Integer modulesCompleted;
    private Integer activitiesDone;        // Combined (map + practice)
    private Integer starsEarned;           // Combined (quest + practice)
    private Integer currentStreakDays;
    private BigDecimal averageScore;

    // Dual Tracking Breakdown (Curriculum vs AI Practice)
    private Integer mapActivitiesDone;
    private Integer practiceActivitiesDone;
    private Integer questStarsEarned;
    private Integer practiceStarsEarned;

    private Map<String, SubjectDetailDTO> subjectProgress;
    private List<BadgeDTO> badges;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubjectDetailDTO {
        private BigDecimal percentage;
        private String color;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BadgeDTO {
        private String id;
        private String name;
        private String icon;
        private Boolean earned;
    }
}
