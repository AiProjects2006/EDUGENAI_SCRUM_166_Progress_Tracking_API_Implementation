package com.ailearning.progressservice.service;

import com.ailearning.progressservice.dto.PlatformAnalyticsOverviewDTO;
import com.ailearning.progressservice.dto.TopicAnalyticsResponseDTO;
import com.ailearning.progressservice.entity.ActivityProgressRecord;
import com.ailearning.progressservice.entity.AggregateTopicAnalytics;
import com.ailearning.progressservice.repository.ActivityProgressRecordRepository;
import com.ailearning.progressservice.repository.AggregateTopicAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AggregateTopicAnalyticsRepository analyticsRepo;
    private final ActivityProgressRecordRepository recordRepo;

    @Transactional(readOnly = true)
    public PlatformAnalyticsOverviewDTO getPlatformAnalyticsOverview() {
        List<ActivityProgressRecord> allRecords = recordRepo.findAll();

        long mapCount = allRecords.stream().filter(r -> "CORE_MAP".equalsIgnoreCase(r.getActivityType())).count();
        long practiceCount = allRecords.stream().filter(r -> "AI_PRACTICE".equalsIgnoreCase(r.getActivityType())).count();
        long totalCount = allRecords.size();

        double avgMapScore = allRecords.stream()
                .filter(r -> "CORE_MAP".equalsIgnoreCase(r.getActivityType()) && r.getScorePercentage() != null)
                .mapToDouble(r -> r.getScorePercentage().doubleValue())
                .average()
                .orElse(0.0);

        double avgPracticeScore = allRecords.stream()
                .filter(r -> "AI_PRACTICE".equalsIgnoreCase(r.getActivityType()) && r.getScorePercentage() != null)
                .mapToDouble(r -> r.getScorePercentage().doubleValue())
                .average()
                .orElse(0.0);

        // Subject Breakdown for the 3 core subjects
        List<String> subjects = List.of("Mathematics", "ICT", "Environmental Studies");
        List<PlatformAnalyticsOverviewDTO.SubjectAnalyticsDTO> subjectList = new ArrayList<>();

        for (String subj : subjects) {
            List<ActivityProgressRecord> subjRecords = allRecords.stream()
                    .filter(r -> subj.equalsIgnoreCase(r.getSubjectName()))
                    .toList();

            long sMap = subjRecords.stream().filter(r -> "CORE_MAP".equalsIgnoreCase(r.getActivityType())).count();
            long sPractice = subjRecords.stream().filter(r -> "AI_PRACTICE".equalsIgnoreCase(r.getActivityType())).count();
            double sAvg = subjRecords.stream()
                    .filter(r -> r.getScorePercentage() != null)
                    .mapToDouble(r -> r.getScorePercentage().doubleValue())
                    .average()
                    .orElse(0.0);

            subjectList.add(PlatformAnalyticsOverviewDTO.SubjectAnalyticsDTO.builder()
                    .subjectName(subj)
                    .totalActivities((long) subjRecords.size())
                    .mapActivities(sMap)
                    .aiGeneratedActivities(sPractice)
                    .averageScore(BigDecimal.valueOf(sAvg).setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        List<TopicAnalyticsResponseDTO> moduleList = analyticsRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PlatformAnalyticsOverviewDTO.builder()
                .totalActivitiesDone(totalCount)
                .mapActivitiesDone(mapCount)
                .aiGeneratedActivitiesDone(practiceCount)
                .averageMapScore(BigDecimal.valueOf(avgMapScore).setScale(2, RoundingMode.HALF_UP))
                .averagePracticeScore(BigDecimal.valueOf(avgPracticeScore).setScale(2, RoundingMode.HALF_UP))
                .subjectBreakdown(subjectList)
                .moduleAnalytics(moduleList)
                .build();
    }

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
        int mapCnt = a.getMapActivitiesCount() != null ? a.getMapActivitiesCount() : 0;
        int aiCnt = a.getAiGeneratedActivitiesCount() != null ? a.getAiGeneratedActivitiesCount() : 0;
        return TopicAnalyticsResponseDTO.builder()
                .analyticsId(a.getAnalyticsId())
                .moduleId(a.getModuleId())
                .courseId(a.getCourseId())
                .subjectName(a.getSubjectName())
                .totalAttempts(a.getTotalAttempts())
                .totalCompletions(a.getTotalCompletions())
                .mapActivitiesCount(mapCnt)
                .aiGeneratedActivitiesCount(aiCnt)
                .averageScore(a.getAverageScore())
                .averageTimeSpentMins(a.getAverageTimeSpentMins())
                .difficultyRating(a.getDifficultyRating())
                .lastUpdated(a.getLastUpdated())
                .build();
    }
}
