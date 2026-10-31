package com.ailearning.progressservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelCompleteRequestDTO {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Course ID cannot be null")
    private Long courseId;

    @NotNull(message = "Module ID cannot be null")
    private Long moduleId;

    @NotNull(message = "Level number cannot be null")
    private Integer levelNumber; // 1 to 6 on Island Quest Map

    @Builder.Default
    private Boolean isBossLevel = false; // true if Kraken Boss (Level 6) beaten

    private BigDecimal scorePercentage;
    private Integer timeSpentMins;
}
