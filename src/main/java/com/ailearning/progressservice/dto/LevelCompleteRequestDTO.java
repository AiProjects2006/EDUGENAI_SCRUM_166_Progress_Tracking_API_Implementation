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

    private Integer totalLevels; // Dynamic total levels for this module/map (e.g. 3, 5, 6), default 6
    private Integer totalModules; // Dynamic total modules for this course (e.g. 2, 4), default 4
    private Integer totalCourses; // Dynamic total courses for this subject (e.g. 2, 5), default 5
    private String subjectName; // e.g. "Mathematics", "ICT", "Environmental Studies"

    private BigDecimal scorePercentage;
    private Integer timeSpentMins;
}
