package com.weeklyreport.backend.controller;

import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.dto.CreateReportRequest;
import com.weeklyreport.backend.dto.ReportContentRequest;
import com.weeklyreport.backend.dto.ReportResponse;
import com.weeklyreport.backend.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * No role gate beyond authentication: row-level ownership in {@link ReportService} is what
 * actually controls access here, not the caller's role.
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse createReport(
            @AuthenticationPrincipal User user, @Valid @RequestBody CreateReportRequest request) {
        return reportService.createReport(user.getId(), request, request.content());
    }

    @PutMapping("/{id}")
    public ReportResponse updateReportContent(
            @AuthenticationPrincipal User user,
            @PathVariable long id,
            @Valid @RequestBody ReportContentRequest request) {
        return reportService.updateReportContent(id, user.getId(), request);
    }

    @PostMapping("/{id}/submit")
    public ReportResponse submitReport(@AuthenticationPrincipal User user, @PathVariable long id) {
        return reportService.submitReport(id, user.getId());
    }
}
