package com.sahasathi.controller;

import com.sahasathi.dto.ApiResponse;
import com.sahasathi.model.Report;
import com.sahasathi.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<Report>> createReport(
            @RequestParam Long reporterId,
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam String reason,
            @RequestParam(required = false) String description) {
        log.info("Creating report: targetType={}, targetId={}, reason={}", targetType, targetId, reason);
        Report report = reportService.createReport(reporterId, targetType, targetId, reason, description);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(report, "Report submitted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Report>>> getReports(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching reports for user: {}", userId);
        Page<Report> reports = reportService.getReportsByUser(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(reports, "Reports fetched successfully"));
    }
}
