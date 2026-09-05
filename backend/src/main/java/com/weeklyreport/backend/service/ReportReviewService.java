package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.Achievement;
import com.weeklyreport.backend.domain.Blocker;
import com.weeklyreport.backend.domain.HoursBreakdown;
import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.ReportStatus;
import com.weeklyreport.backend.domain.ReportVersion;
import com.weeklyreport.backend.domain.ReviewAction;
import com.weeklyreport.backend.domain.ReviewComment;
import com.weeklyreport.backend.domain.TaskEntry;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.dto.ReportResponse;
import com.weeklyreport.backend.dto.ReviewRequest;
import com.weeklyreport.backend.exception.InvalidReportStateException;
import com.weeklyreport.backend.exception.ReportNotFoundException;
import com.weeklyreport.backend.repository.AchievementRepository;
import com.weeklyreport.backend.repository.BlockerRepository;
import com.weeklyreport.backend.repository.HoursBreakdownRepository;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.ReportVersionRepository;
import com.weeklyreport.backend.repository.ReviewCommentRepository;
import com.weeklyreport.backend.repository.TaskEntryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A manager's verdict on a submitted report — the piece that completes the versioning design:
 * approving freezes the current version in place, requesting changes freezes it as commented-on
 * history and opens a new, editable version copied forward from it.
 */
@Service
public class ReportReviewService {

    private final ReportRepository reportRepository;
    private final ReportVersionRepository reportVersionRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final TaskEntryRepository taskEntryRepository;
    private final BlockerRepository blockerRepository;
    private final AchievementRepository achievementRepository;
    private final HoursBreakdownRepository hoursBreakdownRepository;
    private final ReportContentLoader contentLoader;
    private final CurrentReportVersionFinder currentVersionFinder;

    public ReportReviewService(
            ReportRepository reportRepository,
            ReportVersionRepository reportVersionRepository,
            ReviewCommentRepository reviewCommentRepository,
            TaskEntryRepository taskEntryRepository,
            BlockerRepository blockerRepository,
            AchievementRepository achievementRepository,
            HoursBreakdownRepository hoursBreakdownRepository,
            ReportContentLoader contentLoader,
            CurrentReportVersionFinder currentVersionFinder) {
        this.reportRepository = reportRepository;
        this.reportVersionRepository = reportVersionRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.taskEntryRepository = taskEntryRepository;
        this.blockerRepository = blockerRepository;
        this.achievementRepository = achievementRepository;
        this.hoursBreakdownRepository = hoursBreakdownRepository;
        this.contentLoader = contentLoader;
        this.currentVersionFinder = currentVersionFinder;
    }

    @Transactional
    public ReportResponse review(User manager, long reportId, ReviewRequest request) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ReportNotFoundException(reportId));
        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new InvalidReportStateException("Report " + reportId + " is not awaiting review");
        }

        ReportVersion reviewedVersion = currentVersionFinder.get(report);
        recordVerdict(manager, reviewedVersion, request);

        if (request.action() == ReviewAction.APPROVED) {
            report.setStatus(ReportStatus.APPROVED);
            return ReportResponse.from(report, contentLoader.load(reviewedVersion));
        }

        report.setStatus(ReportStatus.NEEDS_CORRECTION);
        ReportVersion newVersion = openNewVersionFrom(report, reviewedVersion);
        return ReportResponse.from(report, contentLoader.load(newVersion));
    }

    private void recordVerdict(User manager, ReportVersion reviewedVersion, ReviewRequest request) {
        ReviewComment reviewComment = new ReviewComment();
        reviewComment.setReportVersionId(reviewedVersion.getId());
        reviewComment.setManager(manager);
        reviewComment.setAction(request.action());
        reviewComment.setComment(request.comment());
        reviewCommentRepository.save(reviewComment);
    }

    /** Opens the next version as a copy of {@code source}'s content and points the report at it. */
    private ReportVersion openNewVersionFrom(Report report, ReportVersion source) {
        ReportVersion newVersion = new ReportVersion();
        newVersion.setReport(report);
        newVersion.setVersionNo(source.getVersionNo() + 1);
        reportVersionRepository.save(newVersion);

        copyContentForward(source, newVersion);
        report.setCurrentVersionNo(newVersion.getVersionNo());
        return newVersion;
    }

    private void copyContentForward(ReportVersion source, ReportVersion target) {
        target.setTasksPlannedNext(source.getTasksPlannedNext());
        target.setNotes(source.getNotes());
        target.setLinks(source.getLinks());

        long sourceId = source.getId();
        long targetId = target.getId();

        List<TaskEntry> taskEntries = taskEntryRepository.findByReportVersionId(sourceId).stream()
                .map(entry -> copyTaskEntry(targetId, entry))
                .toList();
        List<Blocker> blockers = blockerRepository.findByReportVersionId(sourceId).stream()
                .map(blocker -> copyBlocker(targetId, blocker))
                .toList();
        List<Achievement> achievements = achievementRepository.findByReportVersionId(sourceId).stream()
                .map(achievement -> copyAchievement(targetId, achievement))
                .toList();
        List<HoursBreakdown> hoursBreakdowns = hoursBreakdownRepository.findByReportVersionId(sourceId).stream()
                .map(hoursBreakdown -> copyHoursBreakdown(targetId, hoursBreakdown))
                .toList();

        taskEntryRepository.saveAll(taskEntries);
        blockerRepository.saveAll(blockers);
        achievementRepository.saveAll(achievements);
        hoursBreakdownRepository.saveAll(hoursBreakdowns);
    }

    private static TaskEntry copyTaskEntry(long targetVersionId, TaskEntry source) {
        TaskEntry copy = new TaskEntry();
        copy.setReportVersionId(targetVersionId);
        copy.setTaskName(source.getTaskName());
        copy.setPriority(source.getPriority());
        copy.setPlannedPct(source.getPlannedPct());
        copy.setActualPct(source.getActualPct());
        copy.setStatus(source.getStatus());
        copy.setTimePlanned(source.getTimePlanned());
        copy.setTimeSpent(source.getTimeSpent());
        copy.setDeliverable(source.getDeliverable());
        return copy;
    }

    private static Blocker copyBlocker(long targetVersionId, Blocker source) {
        Blocker copy = new Blocker();
        copy.setReportVersionId(targetVersionId);
        copy.setDescription(source.getDescription());
        copy.setKeyIssue(source.isKeyIssue());
        return copy;
    }

    private static Achievement copyAchievement(long targetVersionId, Achievement source) {
        Achievement copy = new Achievement();
        copy.setReportVersionId(targetVersionId);
        copy.setDescription(source.getDescription());
        copy.setKeyHighlight(source.isKeyHighlight());
        return copy;
    }

    private static HoursBreakdown copyHoursBreakdown(long targetVersionId, HoursBreakdown source) {
        HoursBreakdown copy = new HoursBreakdown();
        copy.setReportVersionId(targetVersionId);
        copy.setTaskType(source.getTaskType());
        copy.setHours(source.getHours());
        return copy;
    }
}
