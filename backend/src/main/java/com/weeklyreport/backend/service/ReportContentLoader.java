package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.ReportVersion;
import com.weeklyreport.backend.dto.ReportContentResponse;
import com.weeklyreport.backend.repository.AchievementRepository;
import com.weeklyreport.backend.repository.BlockerRepository;
import com.weeklyreport.backend.repository.HoursBreakdownRepository;
import com.weeklyreport.backend.repository.TaskEntryRepository;
import org.springframework.stereotype.Component;

/**
 * Assembles a {@link ReportContentResponse} for a version. Shared by the write-side
 * {@link ReportService} (whose responses echo back what was just saved) and the read-side
 * {@link ReportQueryService} (report detail), so the four-repository lookup lives in one place.
 */
@Component
class ReportContentLoader {

    private final TaskEntryRepository taskEntryRepository;
    private final BlockerRepository blockerRepository;
    private final AchievementRepository achievementRepository;
    private final HoursBreakdownRepository hoursBreakdownRepository;

    ReportContentLoader(
            TaskEntryRepository taskEntryRepository,
            BlockerRepository blockerRepository,
            AchievementRepository achievementRepository,
            HoursBreakdownRepository hoursBreakdownRepository) {
        this.taskEntryRepository = taskEntryRepository;
        this.blockerRepository = blockerRepository;
        this.achievementRepository = achievementRepository;
        this.hoursBreakdownRepository = hoursBreakdownRepository;
    }

    ReportContentResponse load(ReportVersion version) {
        return ReportContentResponse.from(
                version,
                taskEntryRepository.findByReportVersionId(version.getId()),
                blockerRepository.findByReportVersionId(version.getId()),
                achievementRepository.findByReportVersionId(version.getId()),
                hoursBreakdownRepository.findByReportVersionId(version.getId()));
    }
}
