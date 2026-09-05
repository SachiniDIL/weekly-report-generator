package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.dto.ReportListFilters;
import com.weeklyreport.backend.dto.ReportListItemView;
import com.weeklyreport.backend.dto.ReportResponse;
import com.weeklyreport.backend.dto.ReportVersionHistoryItem;
import com.weeklyreport.backend.dto.ReviewCommentView;
import com.weeklyreport.backend.exception.ReportNotFoundException;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.ReportVersionRepository;
import com.weeklyreport.backend.repository.ReviewCommentRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only, role-scoped access to reports — kept separate from the write-side
 * {@link ReportService} since listing/viewing has a distinct authorization shape (role- and
 * ownership-based visibility) from the create/edit/submit ownership check.
 */
@Service
public class ReportQueryService {

    private final ReportRepository reportRepository;
    private final ReportVersionRepository reportVersionRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReportContentLoader contentLoader;
    private final CurrentReportVersionFinder currentVersionFinder;

    public ReportQueryService(
            ReportRepository reportRepository,
            ReportVersionRepository reportVersionRepository,
            ReviewCommentRepository reviewCommentRepository,
            ReportContentLoader contentLoader,
            CurrentReportVersionFinder currentVersionFinder) {
        this.reportRepository = reportRepository;
        this.reportVersionRepository = reportVersionRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.contentLoader = contentLoader;
        this.currentVersionFinder = currentVersionFinder;
    }

    @Transactional(readOnly = true)
    public Page<ReportListItemView> listReports(User caller, ReportListFilters filters, Pageable pageable) {
        if (caller.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("Admins cannot view reports");
        }

        ReportListFilters scopedFilters = scopeToCaller(caller, filters);
        return reportRepository
                .findAll(ReportSpecifications.matching(scopedFilters), defaultSorted(pageable))
                .map(ReportListItemView::from);
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportDetail(User caller, long reportId) {
        Report report = getVisibleReport(caller, reportId);
        return ReportResponse.from(report, contentLoader.load(currentVersionFinder.get(report)));
    }

    /** Newest first; each version carries its own content and the review comment made against it. */
    @Transactional(readOnly = true)
    public List<ReportVersionHistoryItem> getVersionHistory(User caller, long reportId) {
        Report report = getVisibleReport(caller, reportId);

        return reportVersionRepository.findByReportIdOrderByVersionNoDesc(report.getId()).stream()
                .map(version -> new ReportVersionHistoryItem(
                        contentLoader.load(version),
                        reviewCommentRepository
                                .findByReportVersionId(version.getId())
                                .map(ReviewCommentView::from)
                                .orElse(null)))
                .toList();
    }

    private Report getVisibleReport(User caller, long reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ReportNotFoundException(reportId));
        if (!canView(caller, report)) {
            throw new AccessDeniedException("You cannot view this report");
        }
        return report;
    }

    /**
     * A MEMBER's list is always scoped to their own reports — an explicit request for someone
     * else's is rejected rather than silently overridden. A MANAGER's requested filters (an
     * optional {@code userId} included) are applied as given.
     */
    private ReportListFilters scopeToCaller(User caller, ReportListFilters filters) {
        if (caller.getRole() != Role.MEMBER) {
            return filters;
        }
        if (filters.userId() != null && !filters.userId().equals(caller.getId())) {
            throw new AccessDeniedException("Members can only list their own reports");
        }
        return new ReportListFilters(
                filters.projectId(), filters.status(), filters.weekStart(), filters.weekEnd(), caller.getId());
    }

    private boolean canView(User caller, Report report) {
        return switch (caller.getRole()) {
            case MANAGER -> true;
            case MEMBER -> report.getUser().getId().equals(caller.getId());
            case ADMIN -> false;
        };
    }

    private static Pageable defaultSorted(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").ascending());
    }
}
