package com.ailearning.progressservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicAnalyticsResponseDTO {
    private Long analyticsId;
    private Long moduleId;
    private Long courseId;
    private String subjectName;
    private Integer totalAttempts;
    private Integer totalCompletions;
    private Integer mapActivitiesCount;
    private Integer aiGeneratedActivitiesCount;
    private BigDecimal averageScore;
    private Integer averageTimeSpentMins;
    private String difficultyRating;
    private LocalDateTime lastUpdated;
}
