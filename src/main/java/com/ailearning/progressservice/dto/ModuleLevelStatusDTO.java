package com.ailearning.progressservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleLevelStatusDTO {
    private Integer levelNumber; // 1, 2, 3, 4, 5, 6
    private String status;       // "completed", "current", "locked"
    private Boolean isCompleted;
    private Boolean isLocked;
    private Boolean isBoss;
    private Integer stars;       // 0, 1, 2, 3 stars earned
    private BigDecimal scorePercentage;
    private Integer timeSpentMins;
}

