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
    private Integer coursesCompleted;
    private Integer modulesCompleted;
    private Integer activitiesDone;
    private Integer starsEarned;
    private Integer currentStreakDays;
    private BigDecimal averageScore;
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