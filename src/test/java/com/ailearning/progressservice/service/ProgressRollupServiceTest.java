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
                .totalLevels(6)
                .isCompleted(false)
                .build();
    }

    @Test
    void testRecordQuestLevelComplete_AdvancesLevelAndCountsActivity() {
        ProgressSummary summary = ProgressSummary.builder()
                .userId(101L)
                .activitiesDone(0)
                .starsEarned(0)
                .mapActivitiesDone(0)
                .practiceActivitiesDone(0)
                .questStarsEarned(0)
                .practiceStarsEarned(0)
                .build();

        when(courseProgressService.getOrCreateModuleProgress(101L, 5L, 1L)).thenReturn(mockModuleProgress);
        when(summaryRepo.findById(101L)).thenReturn(Optional.of(summary));

        LevelCompleteRequestDTO req = LevelCompleteRequestDTO.builder()
                .userId(101L)
                .courseId(5L)
                .moduleId(1L)
                .levelNumber(1)
                .totalLevels(6)
                .isBossLevel(false)
                .scorePercentage(BigDecimal.valueOf(90))
                .build();

        rollupService.recordQuestLevelComplete(req);

        assertEquals(2, mockModuleProgress.getCurrentLevelId());
        assertEquals(1, summary.getActivitiesDone());
        assertEquals(1, summary.getMapActivitiesDone());
        assertEquals(0, summary.getPracticeActivitiesDone());
        assertEquals(5, summary.getStarsEarned());
        verify(moduleRepo, times(1)).save(mockModuleProgress);
        verify(summaryRepo, times(1)).save(summary);
        verify(recordRepo, times(1)).save(any());
    }

    @Test
    void testRecordPracticeActivity_AddsStarsAndActivitiesWithoutTouchingCourses() {
        ProgressSummary summary = ProgressSummary.builder()
                .userId(101L)
                .starsEarned(50)
                .activitiesDone(2)
                .mapActivitiesDone(2)
                .practiceActivitiesDone(0)
                .questStarsEarned(50)
                .practiceStarsEarned(0)
                .coursesCompleted(0)
                .modulesCompleted(0)
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
        assertEquals(2, summary.getMapActivitiesDone());
        assertEquals(1, summary.getPracticeActivitiesDone());
        // Verify coursesCompleted and modulesCompleted remain 0!
        assertEquals(0, summary.getCoursesCompleted());
        assertEquals(0, summary.getModulesCompleted());
        verify(summaryRepo, times(1)).save(summary);
        verify(recordRepo, times(1)).save(any());
    }
}
