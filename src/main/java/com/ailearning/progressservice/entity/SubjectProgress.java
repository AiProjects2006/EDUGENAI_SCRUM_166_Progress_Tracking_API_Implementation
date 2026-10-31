package com.ailearning.progressservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subject_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "subject_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_progress_id")
    private Long subjectProgressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private ProgressSummary progressSummary;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "subject_name", nullable = false, length = 50)
    private String subjectName;

    @Column(name = "completed_courses")
    @Builder.Default
    private Integer completedCourses = 0;

    @Column(name = "total_courses")
    @Builder.Default
    private Integer totalCourses = 5;

    @Column(name = "completion_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal completionPercentage = BigDecimal.ZERO;

    @Column(name = "stars_earned")
    @Builder.Default
    private Integer starsEarned = 0;

    @Column(name = "color_code", length = 10)
    private String colorCode;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "subjectProgress", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CourseProgress> courseProgresses = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}