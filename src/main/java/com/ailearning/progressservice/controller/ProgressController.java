package com.ailearning.progressservice.controller;

import com.ailearning.progressservice.dto.*;
import com.ailearning.progressservice.service.ActivityProgressRollupService;
import com.ailearning.progressservice.service.ProgressSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@Tag(name = "Progress Tracking", description = "Endpoints for student progress, subjects, courses, activities, and analytics")
public class ProgressController {

    private final ActivityProgressRollupService rollupService;
    private final ProgressSummaryService summaryService;

    @Operation(summary = "Get full student progress summary for Student UI dashboard")
    @GetMapping("/summary/user/{userId}")
    public ResponseEntity<ApiResponse<ProgressSummaryResponseDTO>> getProgressSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Progress retrieved successfully",
                summaryService.getStudentProgressDashboard(userId)));
    }

    @Operation(summary = "Record AI Practice activity completion (awards bonus stars and subject progress)")
    @PostMapping("/record")
    public ResponseEntity<ApiResponse<String>> recordPracticeActivity(@Valid @RequestBody PracticeActivityRequestDTO req) {
        rollupService.recordPracticeActivity(req);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Practice activity recorded and stars awarded successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Record Island Quest Map level completion (Beating Level 6 Boss completes module)")
    @PostMapping("/activity-map/level-complete")
    public ResponseEntity<ApiResponse<String>> completeQuestLevel(@Valid @RequestBody LevelCompleteRequestDTO req) {
        rollupService.recordQuestLevelComplete(req);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Quest level recorded and progress updated successfully")
                .data(null)
                .build());
    }
}
