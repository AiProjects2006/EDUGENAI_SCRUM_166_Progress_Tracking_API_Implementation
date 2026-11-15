package com.ailearning.progressservice.service;

import com.ailearning.progressservice.dto.StudentBadgeResponseDTO;
import com.ailearning.progressservice.entity.StudentBadge;
import com.ailearning.progressservice.repository.StudentBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final StudentBadgeRepository badgeRepo;
    private final ProgressSummaryService summaryService;

    @Transactional
    public List<StudentBadgeResponseDTO> getStudentBadges(Long userId) {
        summaryService.getOrCreateSummary(userId); // ensure seeded default badges exist
        List<StudentBadge> badges = badgeRepo.findByProgressSummary_UserId(userId);

        return badges.stream()
                .map(b -> StudentBadgeResponseDTO.builder()
                        .badgeRecordId(b.getBadgeRecordId())
                        .badgeCode(b.getBadgeCode())
                        .badgeName(b.getBadgeName())
                        .iconUrl(b.getIconUrl())
                        .isEarned(b.getIsEarned())
                        .earnedAt(b.getEarnedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
