package com.ailearning.progressservice.service;

import com.ailearning.progressservice.dto.ActivityRecordResponseDTO;
import com.ailearning.progressservice.dto.ModuleLevelStatusDTO;
import com.ailearning.progressservice.entity.ActivityProgressRecord;
import com.ailearning.progressservice.entity.ModuleProgress;
import com.ailearning.progressservice.repository.ActivityProgressRecordRepository;
import com.ailearning.progressservice.repository.CourseProgressRepository;
import com.ailearning.progressservice.repository.ModuleProgressRepository;
import com.ailearning.progressservice.repository.SubjectProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseProgressServiceTest {

    @Mock
    private CourseProgressRepository courseRepo;

    @Mock
    private ModuleProgressRepository moduleRepo;

    @Mock
    private SubjectProgressRepository subjectRepo;

    @Mock
    private ActivityProgressRecordRepository recordRepo;

    @Mock
    private ProgressSummaryService summaryService;

    @InjectMocks
    private CourseProgressService courseProgressService;

    private ModuleProgress mockModule;

    @BeforeEach
    void setUp() {
        mockModule = ModuleProgress.builder()
                .moduleId(10L)
                .userId(100L)
                .currentLevelId(2)
                .totalLevels(6)
                .isCompleted(false)
                .build();
    }

    @Test
    void testGetModuleLevels_ReturnsCorrectLevelStatusesAndStars() {
        when(moduleRepo.findByUserIdAndModuleId(100L, 10L)).thenReturn(Optional.of(mockModule));

        ActivityProgressRecord level1Record = ActivityProgressRecord.builder()
                .userId(100L)
                .levelNumber(1)
                .scorePercentage(BigDecimal.valueOf(95.00))
                .isPassed(true)
                .timeSpentMins(4)
                .build();

        when(recordRepo.findByUserIdAndModuleProgress_ModuleId(100L, 10L)).thenReturn(List.of(level1Record));

        List<ModuleLevelStatusDTO> levels = courseProgressService.getModuleLevels(100L, 10L);

        assertEquals(6, levels.size());

        // Level 1: completed, 3 stars (score >= 80%)
        ModuleLevelStatusDTO lvl1 = levels.get(0);
        assertEquals(1, lvl1.getLevelNumber());
        assertEquals("completed", lvl1.getStatus());
        assertTrue(lvl1.getIsCompleted());
        assertEquals(3, lvl1.getStars());
        assertEquals(BigDecimal.valueOf(95.00), lvl1.getScorePercentage());

        // Level 2: current, unlocked
        ModuleLevelStatusDTO lvl2 = levels.get(1);
        assertEquals(2, lvl2.getLevelNumber());
        assertEquals("current", lvl2.getStatus());
        assertFalse(lvl2.getIsCompleted());

        // Level 6: locked, boss
        ModuleLevelStatusDTO lvl6 = levels.get(5);
        assertEquals(6, lvl6.getLevelNumber());
        assertEquals("locked", lvl6.getStatus());
        assertTrue(lvl6.getIsBoss());
    }

    @Test
    void testGetStudentActivityHistory_FiltersByTypeAndSubject() {
        ActivityProgressRecord r1 = ActivityProgressRecord.builder()
                .recordId(1L)
                .userId(100L)
                .activityType("CORE_MAP")
                .subjectName("Mathematics")
                .scorePercentage(BigDecimal.valueOf(90))
                .completedAt(LocalDateTime.now())
                .build();

        ActivityProgressRecord r2 = ActivityProgressRecord.builder()
                .recordId(2L)
                .userId(100L)
                .activityType("CORE_MAP")
                .subjectName("ICT")
                .scorePercentage(BigDecimal.valueOf(80))
                .completedAt(LocalDateTime.now().minusHours(1))
                .build();

        when(recordRepo.findByUserIdAndActivityTypeOrderByCompletedAtDesc(100L, "CORE_MAP"))
                .thenReturn(List.of(r1, r2));

        List<ActivityRecordResponseDTO> result = courseProgressService.getStudentActivityHistory(100L, "CORE_MAP", "Mathematics", 10);

        assertEquals(1, result.size());
        assertEquals("Mathematics", result.get(0).getSubjectName());
        assertEquals("CORE_MAP", result.get(0).getActivityType());
    }
}
