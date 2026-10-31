package com.ailearning.progressservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aggregate_topic_analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregateTopicAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analytics_id")
    private Long analyticsId;

    @Column(name = "module_id", unique = true, nullable = false)
    private Long moduleId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "subject_name", length = 50)
    private String subjectName;

    @Column(name = "total_attempts")
    @Builder.Default
    private Integer totalAttempts = 0;

    @Column(name = "total_completions")
    @Builder.Default
    private Integer totalCompletions = 0;

    @Column(name = "average_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal averageScore = BigDecimal.ZERO;

    @Column(name = "average_time_spent_mins")
    @Builder.Default
    private Integer averageTimeSpentMins = 0;

    @Column(name = "difficulty_rating", length = 20)
    @Builder.Default
    private String difficultyRating = "MEDIUM";

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
