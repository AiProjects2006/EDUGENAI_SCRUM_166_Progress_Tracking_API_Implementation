package com.ailearning.progressservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeActivityRequestDTO {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Activity ID cannot be null")
    private Long activityId;

    private String subjectName; // e.g. "Mathematics", "Science"
    private BigDecimal scorePercentage;
    private Integer starsEarned;
    private Integer timeSpentMins;
}
