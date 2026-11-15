package com.ailearning.progressservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityRecordResponseDTO {
    private Long recordId;
    private Long activityId;
    private String activityType; // "CORE_MAP" or "AI_PRACTICE"
    private Integer levelNumber;
    private String subjectName;
    private Long moduleId;
    private BigDecimal scorePercentage;
    private BigDecimal marksObtained;
    private BigDecimal totalMarks;
    private Boolean isPassed;
    private Integer timeSpentMins;
    private LocalDateTime completedAt;
}

