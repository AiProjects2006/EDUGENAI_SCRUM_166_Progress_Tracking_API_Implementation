package com.ailearning.progressservice.controller;

import com.ailearning.progressservice.dto.*;
import com.ailearning.progressservice.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Progress Tracking", description = "Endpoints for student progress, subjects, courses, activities, and analytics")
public class ProgressController {

    private final ProgressSummaryService summaryService;
    private final CourseProgressService courseProgressService;
    private final ActivityProgressRollupService rollupService;
    private final BadgeService badgeService;
    private final AnalyticsService analyticsService;

    // 1. Dashboard Summary
    @GetMapping("/summary/user/{userId}")
    @Operation(summary = "Get full student progress summary for Student UI dashboard")
    public ResponseEntity<ApiResponse<ProgressSummaryResponseDTO>> getProgressSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Progress retrieved successfully",
                summaryService.getStudentProgressDashboard(userId)));
    }

    // 2. Course Progress
    @GetMapping("/courses/{courseId}/user/{userId}")
    @Operation(summary = "Get progress for a specific course")
    public ResponseEntity<ApiResponse<CourseProgressResponseDTO>> getCourseProgress(
            @PathVariable Long courseId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Course progress retrieved",
                courseProgressService.getCourseProgress(userId, courseId)));
    }

    // 3. Module Progress (Quest Map Active Level)
    @GetMapping("/modules/{moduleId}/user/{userId}")
    @Operation(summary = "Get module progress including Island Quest Map current level")
    public ResponseEntity<ApiResponse<ModuleProgressResponseDTO>> getModuleProgress(
            @PathVariable Long moduleId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Module progress retrieved",
                courseProgressService.getModuleProgress(userId, moduleId)));
    }

    // 4. Badges
    @GetMapping("/badges/user/{userId}")
    @Operation(summary = "Get badges earned and locked for student")
    public ResponseEntity<ApiResponse<List<StudentBadgeResponseDTO>>> getStudentBadges(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Badges retrieved",
                badgeService.getStudentBadges(userId)));
    }

    // 5. Submit Island Quest Map Level (Beating Level 6 completes Module & rolls up)
    @PostMapping("/activity-map/level-complete")
    @Operation(summary = "Record Island Quest Map level completion (Beating Level 6 Boss completes module)")
    public ResponseEntity<ApiResponse<String>> completeQuestLevel(@Valid @RequestBody LevelCompleteRequestDTO dto) {
        rollupService.recordQuestLevelComplete(dto);
        return ResponseEntity.ok(ApiResponse.success("Quest level recorded and progress updated successfully", null));
    }

    // 6. Submit AI Practice Quiz Activity
    @PostMapping("/record")
    @Operation(summary = "Record AI Practice activity completion (awards bonus stars and subject progress)")
    public ResponseEntity<ApiResponse<String>> recordPracticeActivity(@Valid @RequestBody PracticeActivityRequestDTO dto) {
        rollupService.recordPracticeActivity(dto);
        return ResponseEntity.ok(ApiResponse.success("Practice activity recorded and stars awarded successfully", null));
    }

    // 7. Backoffice Analytics Overview for Creators & Admins (Map vs Generated vs Combined)
    @GetMapping("/analytics/overview")
    @Operation(summary = "Get aggregate platform performance analytics (Map vs AI Generated vs Combined) for Admins and Content Creators")
    public ResponseEntity<ApiResponse<PlatformAnalyticsOverviewDTO>> getAnalyticsOverview() {
        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved successfully",
                analyticsService.getPlatformAnalyticsOverview()));
    }

    // 8. Backoffice Module Analytics
    @GetMapping("/analytics/module/{moduleId}")
    @Operation(summary = "Get analytics for a specific module")
    public ResponseEntity<ApiResponse<TopicAnalyticsResponseDTO>> getModuleAnalytics(@PathVariable Long moduleId) {
        return analyticsService.getModuleAnalytics(moduleId)
                .map(a -> ResponseEntity.ok(ApiResponse.success("Module analytics retrieved", a)))
                .orElse(ResponseEntity.ok(ApiResponse.error("No analytics found for this module")));
    }
}
