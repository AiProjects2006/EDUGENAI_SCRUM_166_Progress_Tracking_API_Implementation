package com.ailearning.progressservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_badge", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "badge_code" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_record_id")
    private Long badgeRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private ProgressSummary progressSummary;

    @Column(name = "badge_code", nullable = false, length = 50)
    private String badgeCode;

    @Column(name = "badge_name", nullable = false, length = 100)
    private String badgeName;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "is_earned")
    @Builder.Default
    private Boolean isEarned = false;

    @Column(name = "earned_at")
    private LocalDateTime earnedAt;
}