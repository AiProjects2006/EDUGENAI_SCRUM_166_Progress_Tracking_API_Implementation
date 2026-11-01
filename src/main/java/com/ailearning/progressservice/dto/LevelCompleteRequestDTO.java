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
    private Integer levelNumber; // 1 to totalLevels on Island Quest Map

    @Builder.Default
    private Boolean isBossLevel = false; // true if Kraken Boss (final level) beaten

    private Integer totalLevels; // Dynamic total levels for this module/map (e.g. 6, 7, 10), optional, default 6

    private BigDecimal scorePercentage;
    private Integer timeSpentMins;
}
