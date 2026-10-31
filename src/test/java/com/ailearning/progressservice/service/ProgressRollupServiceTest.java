package com.ailearning.progressservice.service;

import com.ailearning.progressservice.dto.LevelCompleteRequestDTO;
import com.ailearning.progressservice.dto.PracticeActivityRequestDTO;
import com.ailearning.progressservice.entity.ModuleProgress;
import com.ailearning.progressservice.entity.ProgressSummary;
import com.ailearning.progressservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProgressRollupServiceTest {

    @Mock
    private CourseProgressService courseProgressService;

    @Mock
    private ModuleProgressRepository moduleRepo;

    @Mock
    private CourseProgressRepository courseRepo;

    @Mock
    private SubjectProgressRepository subjectRepo;

    @Mock
    private ProgressSummaryRepository summaryRepo;

    @Mock
    private ActivityProgressRecordRepository recordRepo;

    @Mock
    private StudentBadgeRepository badgeRepo;

    @Mock
    private AggregateTopicAnalyticsRepository analyticsRepo;

    @InjectMocks
    private ActivityProgressRollupService rollupService;

    private ModuleProgress mockModuleProgress;

    @BeforeEach
    void setUp() {
        mockModuleProgress = ModuleProgress.builder()
                .moduleProgressId(1L)
                .userId(101L)
                .moduleId(1L)
                .currentLevelId(1)
                .isCompleted(false)
                .build();
    }

    @Test
    void testRecordQuestLevelComplete_AdvancesLevel() {
        when(courseProgressService.getOrCreateModuleProgress(101L, 5L, 1L)).thenReturn(mockModuleProgress);

        LevelCompleteRequestDTO req = LevelCompleteRequestDTO.builder()
                .userId(101L)
                .courseId(5L)
                .moduleId(1L)
                .levelNumber(1)
                .isBossLevel(false)
                .scorePercentage(BigDecimal.valueOf(90))
                .build();

        rollupService.recordQuestLevelComplete(req);

        assertEquals(2, mockModuleProgress.getCurrentLevelId());
        verify(moduleRepo, times(1)).save(mockModuleProgress);
        verify(recordRepo, times(1)).save(any());
    }

    @Test
    void testRecordPracticeActivity_AddsStarsAndActivities() {
        ProgressSummary summary = ProgressSummary.builder()
                .userId(101L)
                .starsEarned(50)
                .activitiesDone(2)
                .build();

        when(summaryRepo.findById(101L)).thenReturn(Optional.of(summary));

        PracticeActivityRequestDTO req = PracticeActivityRequestDTO.builder()
                .userId(101L)
                .activityId(200L)
                .starsEarned(30)
                .scorePercentage(BigDecimal.valueOf(85))
                .subjectName("Mathematics")
                .build();

        rollupService.recordPracticeActivity(req);

        assertEquals(80, summary.getStarsEarned());
        assertEquals(3, summary.getActivitiesDone());
        verify(summaryRepo, times(1)).save(summary);
        verify(recordRepo, times(1)).save(any());
    }
}
