package com.ailearning.progressservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "course_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_progress_id", nullable = false)
    @JsonIgnore
    private SubjectProgress subjectProgress;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "completed_modules")
    @Builder.Default
    private Integer completedModules = 0;

    @Column(name = "total_modules")
    @Builder.Default
    private Integer totalModules = 0;

    @Column(name = "completion_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal completionPercentage = BigDecimal.ZERO;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "IN_PROGRESS";

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "courseProgress", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ModuleProgress> moduleProgresses = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}