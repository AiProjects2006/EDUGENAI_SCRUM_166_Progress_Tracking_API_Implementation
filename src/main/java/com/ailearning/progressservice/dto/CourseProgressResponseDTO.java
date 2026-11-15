package com.ailearning.progressservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseProgressResponseDTO {
    private Long courseId;
    private Integer completedModules;
    private Integer totalModules;
    private BigDecimal completionPercentage;
    private String status;
}