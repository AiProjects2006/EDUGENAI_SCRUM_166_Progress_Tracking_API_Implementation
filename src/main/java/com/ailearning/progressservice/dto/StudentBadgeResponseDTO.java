package com.ailearning.progressservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentBadgeResponseDTO {
    private Long badgeRecordId;
    private String badgeCode;
    private String badgeName;
    private String iconUrl;
    private Boolean isEarned;
    private LocalDateTime earnedAt;
}
