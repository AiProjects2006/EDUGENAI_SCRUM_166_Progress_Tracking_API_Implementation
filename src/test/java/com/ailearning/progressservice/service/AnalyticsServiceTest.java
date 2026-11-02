package com.ailearning.progressservice.service;

import com.ailearning.progressservice.dto.CreatorAnalyticsReportsDTO;
import com.ailearning.progressservice.dto.CreatorStudentEngagementDTO;
import com.ailearning.progressservice.entity.ActivityProgressRecord;
import com.ailearning.progressservice.entity.ProgressSummary;
import com.ailearning.progressservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AggregateTopicAnalyticsRepository analyticsRepo;

    @Mock
    private ActivityProgressRecordRepository recordRepo;

    @Mock
    private ProgressSummaryRepository summaryRepo;

    @Mock
    private CourseProgressRepository courseRepo;

    @Mock
    private ModuleProgressRepository moduleRepo;

    @Mock
    private StudentBadgeRepository badgeRepo;

    @Mock
    private SubjectProgressRepository subjectRepo;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void testGetCreatorStudentEngagement_UsesEnvironmentalStudiesAndNoScience() {
        when(recordRepo.findAll()).thenReturn(Collections.emptyList());
        when(summaryRepo.findAll()).thenReturn(Collections.emptyList());
        when(courseRepo.findAll()).thenReturn(Collections.emptyList());

        CreatorStudentEngagementDTO result = analyticsService.getCreatorStudentEngagement();

        assertNotNull(result);
        assertNotNull(result.getWeeklyEngagementHours());
        assertNotNull(result.getCompletionRate());
        assertEquals(1240L, result.getActiveStudentsCount());

        // Verify subjects: Must contain "Environmental Studies", "Mathematics", "ICT" and NOT "Science"
        List<String> subjects = result.getTimeSpentPerSubject().stream()
                .map(CreatorStudentEngagementDTO.SubjectTimeDistributionDTO::getSubject)
                .toList();

        assertTrue(subjects.contains("Environmental Studies"), "Must contain Environmental Studies");
        assertTrue(subjects.contains("Mathematics"), "Must contain Mathematics");
        assertTrue(subjects.contains("ICT"), "Must contain ICT");
        assertFalse(subjects.contains("Science"), "Must NOT contain Science");

        // Verify retention risks has idle students
        assertFalse(result.getRetentionRisks().isEmpty());
        assertEquals("Julian Thorne", result.getRetentionRisks().get(0).getName());
        assertEquals("Environmental Studies", result.getRetentionRisks().get(0).getSubject());

        // Verify engaging lessons has Environmental Studies
        assertTrue(result.getMostEngagingLessons().stream()
                .anyMatch(l -> "Environmental Studies".equals(l.getSubject())));
    }

    @Test
    void testGetCreatorAnalyticsReports_UsesEnvironmentalStudiesAndComputesMetrics() {
        ActivityProgressRecord r1 = ActivityProgressRecord.builder()
                .recordId(1L)
                .userId(100L)
                .activityId(101L)
                .subjectName("Environmental Studies")
                .scorePercentage(BigDecimal.valueOf(90.0))
                .timeSpentMins(5)
                .completedAt(LocalDateTime.now())
                .build();

        when(recordRepo.findAll()).thenReturn(List.of(r1));
        when(courseRepo.findAll()).thenReturn(Collections.emptyList());
        when(badgeRepo.countByIsEarnedTrue()).thenReturn(15L);

        CreatorAnalyticsReportsDTO result = analyticsService.getCreatorAnalyticsReports();

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(90.0), result.getAverageGrade());
        assertEquals(BigDecimal.valueOf(76.8), result.getCompletionRate());
        assertEquals(15L, result.getBadgesIssued());
        assertTrue(result.getActiveLearningMinutes() >= 42500L);

        // Verify subjects in subjectPerformance
        List<String> subjects = result.getSubjectPerformance().stream()
                .map(CreatorAnalyticsReportsDTO.SubjectPerformanceDTO::getSubject)
                .toList();
        assertTrue(subjects.contains("Environmental Studies"), "Must contain Environmental Studies");
        assertTrue(subjects.contains("Mathematics"), "Must contain Mathematics");
        assertTrue(subjects.contains("ICT"), "Must contain ICT");
        assertFalse(subjects.contains("Science"), "Must NOT contain Science");

        // Verify grade distribution bell curve has 5 buckets
        assertEquals(5, result.getGradeDistribution().getBuckets().size());

        // Verify daily learning minutes has 7 days
        assertEquals(7, result.getActiveLearningMinutesByDay().size());

        // Verify curriculum effectiveness has items
        assertFalse(result.getCurriculumEffectiveness().isEmpty());
    }
}
