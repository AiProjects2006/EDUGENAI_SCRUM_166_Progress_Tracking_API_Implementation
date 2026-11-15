package com.ailearning.progressservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_progress_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityProgressRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_progress_id")
    @JsonIgnore
    private ModuleProgress moduleProgress;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "level_number")
    private Integer levelNumber;

    @Column(name = "activity_type", length = 30)
    @Builder.Default
    private String activityType = "CORE_MAP"; // CORE_MAP or AI_PRACTICE

    @Column(name = "subject_name", length = 50)
    private String subjectName; // e.g. "Mathematics", "ICT", "Environmental Studies"

    @Column(name = "score_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal scorePercentage;

    @Column(name = "marks_obtained", precision = 5, scale = 2, nullable = false)
    private BigDecimal marksObtained;

    @Column(name = "total_marks", precision = 5, scale = 2, nullable = false)
    private BigDecimal totalMarks;

    @Column(name = "is_passed")
    @Builder.Default
    private Boolean isPassed = true;

    @Column(name = "time_spent_mins")
    @Builder.Default
    private Integer timeSpentMins = 0;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    public void onCreate() {
        this.completedAt = LocalDateTime.now();
    }
}
