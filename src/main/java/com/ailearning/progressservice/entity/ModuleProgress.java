package com.ailearning.progressservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "module_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "progress_id", "module_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "module_progress_id")
    private Long moduleProgressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progress_id", nullable = false)
    @JsonIgnore
    private CourseProgress courseProgress;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "current_level_id")
    @Builder.Default
    private Integer currentLevelId = 1; // 1 to totalLevels on Island Quest Map

    @Column(name = "total_levels")
    @Builder.Default
    private Integer totalLevels = 6;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "time_spent_mins")
    @Builder.Default
    private Integer timeSpentMins = 0;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "moduleProgress", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ActivityProgressRecord> activityRecords = new ArrayList<>();
}
