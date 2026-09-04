package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.Achievement;
import com.weeklyreport.backend.domain.Blocker;
import com.weeklyreport.backend.domain.HoursBreakdown;
import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.ReportStatus;
import com.weeklyreport.backend.domain.ReportVersion;
import com.weeklyreport.backend.domain.TaskEntry;
import com.weeklyreport.backend.dto.CreateReportRequest;
import com.weeklyreport.backend.dto.ReportContentRequest;
import com.weeklyreport.backend.dto.ReportContentResponse;
import com.weeklyreport.backend.dto.ReportResponse;
import com.weeklyreport.backend.exception.DuplicateKeyContentItemException;
import com.weeklyreport.backend.exception.InvalidReportStateException;
import com.weeklyreport.backend.exception.ReportNotFoundException;
import com.weeklyreport.backend.repository.AchievementRepository;
import com.weeklyreport.backend.repository.BlockerRepository;
import com.weeklyreport.backend.repository.HoursBreakdownRepository;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.ReportVersionRepository;
import com.weeklyreport.backend.repository.TaskEntryRepository;
import java.time.Clock;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Draft/edit/submit lifecycle for a report's first version. A report and its version 1 are
 * created together; editing replaces that version's content wholesale in place; submitting
 * stamps it as final. Turning a correction cycle into a genuinely new version is a later step's
 * concern, not this one's.
 */
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportVersionRepository reportVersionRepository;
    private final TaskEntryRepository taskEntryRepository;
    private final BlockerRepository blockerRepository;
    private final AchievementRepository achievementRepository;
    private final HoursBreakdownRepository hoursBreakdownRepository;
    private final Clock clock;

    public ReportService(
            ReportRepository reportRepository,
            ReportVersionRepository reportVersionRepository,
            TaskEntryRepository taskEntryRepository,
            BlockerRepository blockerRepository,
            AchievementRepository achievementRepository,
            HoursBreakdownRepository hoursBreakdownRepository,
            Clock clock) {
        this.reportRepository = reportRepository;
        this.reportVersionRepository = reportVersionRepository;
        this.taskEntryRepository = taskEntryRepository;
        this.blockerRepository = blockerRepository;
        this.achievementRepository = achievementRepository;
        this.hoursBreakdownRepository = hoursBreakdownRepository;
        this.clock = clock;
    }

    @Transactional
    public ReportResponse createReport(long userId, CreateReportRequest request, ReportContentRequest content) {
        Report report = new Report();
        report.setUserId(userId);
        report.setProjectId(request.projectId());
        report.setWeekStart(request.weekStart());
        report.setWeekEnd(request.weekEnd());
        report.setCurrentVersionNo(1);
        reportRepository.save(report);

        ReportVersion version = new ReportVersion();
        version.setReport(report);
        version.setVersionNo(1);
        reportVersionRepository.save(version);

        replaceContent(version, orEmpty(content));
        return toResponse(report, version);
    }

    @Transactional
    public ReportResponse updateReportContent(long reportId, long userId, ReportContentRequest content) {
        Report report = getOwnedReport(reportId, userId);
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new InvalidReportStateException("Report " + reportId + " is not in an editable state");
        }

        ReportVersion version = getCurrentVersion(report);
        replaceContent(version, content);
        return toResponse(report, version);
    }

    @Transactional
    public ReportResponse submitReport(long reportId, long userId) {
        Report report = getOwnedReport(reportId, userId);
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new InvalidReportStateException("Report " + reportId + " is not in a submittable state");
        }

        ReportVersion version = getCurrentVersion(report);
        version.setSubmittedAt(clock.instant());
        report.setStatus(ReportStatus.SUBMITTED);
        return toResponse(report, version);
    }

    /** Deletes and re-inserts every child row for the version — a full replace, not a diff. */
    private void replaceContent(ReportVersion version, ReportContentRequest content) {
        long versionId = version.getId();

        taskEntryRepository.deleteByReportVersionId(versionId);
        blockerRepository.deleteByReportVersionId(versionId);
        achievementRepository.deleteByReportVersionId(versionId);
        hoursBreakdownRepository.deleteByReportVersionId(versionId);

        version.setTasksPlannedNext(content.tasksPlannedNext());
        version.setNotes(content.notes());
        version.setLinks(content.links());

        List<TaskEntry> taskEntries =
                nullToEmpty(content.taskEntries()).stream().map(item -> toTaskEntry(versionId, item)).toList();
        List<Blocker> blockers =
                nullToEmpty(content.blockers()).stream().map(item -> toBlocker(versionId, item)).toList();
        List<Achievement> achievements = nullToEmpty(content.achievements()).stream()
                .map(item -> toAchievement(versionId, item))
                .toList();
        List<HoursBreakdown> hoursBreakdowns = nullToEmpty(content.hoursBreakdown()).stream()
                .map(item -> toHoursBreakdown(versionId, item))
                .toList();

        try {
            taskEntryRepository.saveAll(taskEntries);
            blockerRepository.saveAll(blockers);
            achievementRepository.saveAll(achievements);
            hoursBreakdownRepository.saveAll(hoursBreakdowns);
            // Forces the pending inserts to hit the DB now, inside this try block, so a partial
            // unique index violation surfaces here rather than at some later, unrelated flush.
            reportVersionRepository.flush();
        } catch (DataIntegrityViolationException keyMarkerConstraintViolation) {
            throw new DuplicateKeyContentItemException();
        }
    }

    private ReportResponse toResponse(Report report, ReportVersion version) {
        ReportContentResponse content = ReportContentResponse.from(
                version,
                taskEntryRepository.findByReportVersionId(version.getId()),
                blockerRepository.findByReportVersionId(version.getId()),
                achievementRepository.findByReportVersionId(version.getId()),
                hoursBreakdownRepository.findByReportVersionId(version.getId()));
        return ReportResponse.from(report, content);
    }

    private Report getOwnedReport(long reportId, long userId) {
        Report report = getReport(reportId);
        if (!report.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not own this report");
        }
        return report;
    }

    private Report getReport(long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ReportNotFoundException(id));
    }

    private ReportVersion getCurrentVersion(Report report) {
        return reportVersionRepository
                .findByReportIdAndVersionNo(report.getId(), report.getCurrentVersionNo())
                .orElseThrow(() -> new IllegalStateException(
                        "Report " + report.getId() + " has no version " + report.getCurrentVersionNo()));
    }

    private static ReportContentRequest orEmpty(ReportContentRequest content) {
        return content != null ? content : ReportContentRequest.empty();
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list != null ? list : List.of();
    }

    private static TaskEntry toTaskEntry(long versionId, ReportContentRequest.TaskEntryRequest request) {
        TaskEntry entry = new TaskEntry();
        entry.setReportVersionId(versionId);
        entry.setTaskName(request.taskName());
        entry.setPriority(request.priority());
        entry.setPlannedPct(request.plannedPct());
        entry.setActualPct(request.actualPct());
        entry.setStatus(request.status());
        entry.setTimePlanned(request.timePlanned());
        entry.setTimeSpent(request.timeSpent());
        entry.setDeliverable(request.deliverable());
        return entry;
    }

    private static Blocker toBlocker(long versionId, ReportContentRequest.BlockerRequest request) {
        Blocker blocker = new Blocker();
        blocker.setReportVersionId(versionId);
        blocker.setDescription(request.description());
        blocker.setKeyIssue(request.isKeyIssue());
        return blocker;
    }

    private static Achievement toAchievement(long versionId, ReportContentRequest.AchievementRequest request) {
        Achievement achievement = new Achievement();
        achievement.setReportVersionId(versionId);
        achievement.setDescription(request.description());
        achievement.setKeyHighlight(request.isKeyHighlight());
        return achievement;
    }

    private static HoursBreakdown toHoursBreakdown(
            long versionId, ReportContentRequest.HoursBreakdownRequest request) {
        HoursBreakdown hoursBreakdown = new HoursBreakdown();
        hoursBreakdown.setReportVersionId(versionId);
        hoursBreakdown.setTaskType(request.taskType());
        hoursBreakdown.setHours(request.hours());
        return hoursBreakdown;
    }
}
