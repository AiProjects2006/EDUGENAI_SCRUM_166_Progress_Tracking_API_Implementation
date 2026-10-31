package com.ailearning.progressservice.service;

import com.ailearning.progressservice.dto.TopicAnalyticsResponseDTO;
import com.ailearning.progressservice.entity.AggregateTopicAnalytics;
import com.ailearning.progressservice.repository.AggregateTopicAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AggregateTopicAnalyticsRepository analyticsRepo;

    @Transactional(readOnly = true)
    public List<TopicAnalyticsResponseDTO> getAllTopicAnalytics() {
        return analyticsRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TopicAnalyticsResponseDTO> getModuleAnalytics(Long moduleId) {
        return analyticsRepo.findByModuleId(moduleId).map(this::mapToDTO);
    }

    private TopicAnalyticsResponseDTO mapToDTO(AggregateTopicAnalytics a) {
        return TopicAnalyticsResponseDTO.builder()
                .analyticsId(a.getAnalyticsId())
                .moduleId(a.getModuleId())
                .courseId(a.getCourseId())
                .subjectName(a.getSubjectName())
                .totalAttempts(a.getTotalAttempts())
                .totalCompletions(a.getTotalCompletions())
                .averageScore(a.getAverageScore())
                .averageTimeSpentMins(a.getAverageTimeSpentMins())
                .difficultyRating(a.getDifficultyRating())
                .lastUpdated(a.getLastUpdated())
                .build();
    }
}
