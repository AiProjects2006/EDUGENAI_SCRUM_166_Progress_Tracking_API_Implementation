package com.ailearning.progressservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleProgressResponseDTO {
    private Long moduleId;
    private Integer currentLevelId; // 1 to 6 Boss
    private Boolean isCompleted;
    private Integer timeSpentMins;
}