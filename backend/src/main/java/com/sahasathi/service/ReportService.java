package com.sahasathi.service;

import com.sahasathi.exception.BadRequestException;
import com.sahasathi.exception.ResourceNotFoundException;
import com.sahasathi.model.Report;
import com.sahasathi.model.ReportReason;
import com.sahasathi.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional
    public Report createReport(Long reporterId, String targetType, Long targetId,
                               String reason, String description) {
        ReportReason reportReason;
        try {
            reportReason = ReportReason.valueOf(reason.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid report reason: " + reason);
        }

        Report report = Report.builder()
                .reporterId(reporterId)
                .targetType(targetType.toUpperCase())
                .targetId(targetId)
                .reason(reportReason)
                .description(description)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        Report saved = reportRepository.save(report);
        log.info("Report created: {} by user {} for {}#{}",
                saved.getId(), reporterId, targetType, targetId);
        return saved;
    }

    public Page<Report> getReportsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<Report> getReportsByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }
}
