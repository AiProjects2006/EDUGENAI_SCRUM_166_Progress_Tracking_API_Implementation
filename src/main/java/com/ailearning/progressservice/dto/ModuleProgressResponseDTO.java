package com.ailearning.progressservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleProgressResponseDTO {
    private Long moduleId;
    private Integer currentLevelId; // Active level on Island Quest Map
    private Integer totalLevels;    // Total levels in this module/map (dynamic: 6, 7, 10, etc.)
    private Boolean isCompleted;
    private Integer timeSpentMins;
}
