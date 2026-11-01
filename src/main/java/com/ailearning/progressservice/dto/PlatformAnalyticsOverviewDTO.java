package com.ailearning.progressservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformAnalyticsOverviewDTO {
    // 1. High-Level Platform Totals
    private Long totalActivitiesDone;          // Combined
    private Long mapActivitiesDone;            // Map
    private Long aiGeneratedActivitiesDone;    // AI Generated
    private BigDecimal averageMapScore;
    private BigDecimal averagePracticeScore;

    // 2. Subject Breakdown (Map vs Generated vs Combined)
    private List<SubjectAnalyticsDTO> subjectBreakdown;

    // 3. Module Breakdown
    private List<TopicAnalyticsResponseDTO> moduleAnalytics;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubjectAnalyticsDTO {
        private String subjectName;
        private Long totalActivities;
        private Long mapActivities;
        private Long aiGeneratedActivities;
        private BigDecimal averageScore;
    }
}

