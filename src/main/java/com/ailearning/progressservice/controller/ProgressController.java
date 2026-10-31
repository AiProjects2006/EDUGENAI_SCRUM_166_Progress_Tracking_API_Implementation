package com.ailearning.progressservice.controller;

import com.ailearning.progressservice.dto.ProgressSummaryResponseDTO;
import com.ailearning.progressservice.service.ProgressSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Progress Tracking", description = "Endpoints for student progress, subjects, and stats")
public class ProgressController {

    private final ProgressSummaryService summaryService;

    @GetMapping("/summary/user/{userId}")
    @Operation(summary = "Get full student progress summary for Student UI dashboard")
    public ResponseEntity<ProgressSummaryResponseDTO> getProgressSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(summaryService.getStudentProgressDashboard(userId));
    }
}