package com.ailearning.progressservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "progress_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressSummary {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "courses_completed")
    @Builder.Default
    private Integer coursesCompleted = 0;

    @Column(name = "modules_completed")
    @Builder.Default
    private Integer modulesCompleted = 0;

    @Column(name = "activities_done")
    @Builder.Default
    private Integer activitiesDone = 0;

    @Column(name = "stars_earned")
    @Builder.Default
    private Integer starsEarned = 0;

    @Column(name = "current_streak_days")
    @Builder.Default
    private Integer currentStreakDays = 1;

    @Column(name = "total_time_spent_mins")
    @Builder.Default
    private Integer totalTimeSpentMins = 0;

    @Column(name = "average_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal averageScore = BigDecimal.ZERO;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "progressSummary", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubjectProgress> subjectProgresses = new ArrayList<>();

    @OneToMany(mappedBy = "progressSummary", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudentBadge> badges = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.lastActiveDate == null) {
            this.lastActiveDate = LocalDate.now();
        }
    }
}