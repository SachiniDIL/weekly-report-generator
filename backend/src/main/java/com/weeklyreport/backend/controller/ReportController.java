package com.weeklyreport.backend.controller;

import com.weeklyreport.backend.domain.ReportStatus;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.dto.CreateReportRequest;
import com.weeklyreport.backend.dto.ReportContentRequest;
import com.weeklyreport.backend.dto.ReportListFilters;
import com.weeklyreport.backend.dto.ReportListItemView;
import com.weeklyreport.backend.dto.ReportResponse;
import com.weeklyreport.backend.service.ReportQueryService;
import com.weeklyreport.backend.service.ReportService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * No role gate beyond authentication: row-level ownership (write endpoints) and role-based
 * scoping (list/detail) inside the services are what actually control access here, not the
 * caller's role.
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportQueryService reportQueryService;

    public ReportController(ReportService reportService, ReportQueryService reportQueryService) {
        this.reportService = reportService;
        this.reportQueryService = reportQueryService;
    }

    @GetMapping
    public Page<ReportListItemView> listReports(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd,
            @RequestParam(required = false) Long userId,
            Pageable pageable) {
        ReportListFilters filters = new ReportListFilters(projectId, status, weekStart, weekEnd, userId);
        return reportQueryService.listReports(user, filters, pageable);
    }

    @GetMapping("/{id}")
    public ReportResponse getReportDetail(@AuthenticationPrincipal User user, @PathVariable long id) {
        return reportQueryService.getReportDetail(user, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse createReport(
            @AuthenticationPrincipal User user, @Valid @RequestBody CreateReportRequest request) {
        return reportService.createReport(user, request, request.content());
    }

    @PutMapping("/{id}")
    public ReportResponse updateReportContent(
            @AuthenticationPrincipal User user,
            @PathVariable long id,
            @Valid @RequestBody ReportContentRequest request) {
        return reportService.updateReportContent(id, user, request);
    }

    @PostMapping("/{id}/submit")
    public ReportResponse submitReport(@AuthenticationPrincipal User user, @PathVariable long id) {
        return reportService.submitReport(id, user);
    }
}
