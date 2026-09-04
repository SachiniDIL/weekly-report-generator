package com.weeklyreport.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Achievement;
import com.weeklyreport.backend.domain.Blocker;
import com.weeklyreport.backend.domain.HoursBreakdown;
import com.weeklyreport.backend.domain.Project;
import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.ReportStatus;
import com.weeklyreport.backend.domain.ReportVersion;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.TaskEntry;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportVersionRepository reportVersionRepository;

    @Autowired
    private TaskEntryRepository taskEntryRepository;

    @Autowired
    private BlockerRepository blockerRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private HoursBreakdownRepository hoursBreakdownRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clearTables() {
        deleteAllInFkOrder();
    }

    // The users/projects rows this class creates would otherwise survive into UserRepositoryTest
    // / ProjectRepositoryTest and break their own deleteAll() via the reports FK.
    @AfterEach
    void cleanUpAfterSelf() {
        deleteAllInFkOrder();
    }

    private void deleteAllInFkOrder() {
        hoursBreakdownRepository.deleteAll();
        achievementRepository.deleteAll();
        blockerRepository.deleteAll();
        taskEntryRepository.deleteAll();
        reportVersionRepository.deleteAll();
        reportRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void savesAndReloadsAFullReportVersionChain() {
        Report report = saveReport(saveUser(), saveProject());

        ReportVersion version = new ReportVersion();
        version.setReport(report);
        version.setVersionNo(1);
        version.setTasksPlannedNext("Finish the API integration");
        version.setNotes("On track");
        version.setLinks("https://example.com/board");
        Long versionId = reportVersionRepository.save(version).getId();

        TaskEntry taskEntry = new TaskEntry();
        taskEntry.setReportVersionId(versionId);
        taskEntry.setTaskName("Implement CORS config");
        taskEntry.setPriority("HIGH");
        taskEntry.setPlannedPct(100);
        taskEntry.setActualPct(80);
        taskEntry.setStatus("IN_PROGRESS");
        taskEntry.setTimePlanned(8);
        taskEntry.setTimeSpent(6);
        taskEntry.setDeliverable("Working preflight response");
        Long taskEntryId = taskEntryRepository.save(taskEntry).getId();

        Blocker blocker = new Blocker();
        blocker.setReportVersionId(versionId);
        blocker.setDescription("Waiting on staging credentials");
        blocker.setKeyIssue(true);
        Long blockerId = blockerRepository.save(blocker).getId();

        Achievement achievement = new Achievement();
        achievement.setReportVersionId(versionId);
        achievement.setDescription("Shipped the CORS fix");
        achievement.setKeyHighlight(true);
        Long achievementId = achievementRepository.save(achievement).getId();

        HoursBreakdown hoursBreakdown = new HoursBreakdown();
        hoursBreakdown.setReportVersionId(versionId);
        hoursBreakdown.setTaskType("DEVELOPMENT");
        hoursBreakdown.setHours(new BigDecimal("6.5"));
        Long hoursBreakdownId = hoursBreakdownRepository.save(hoursBreakdown).getId();

        ReportVersion reloadedVersion = reportVersionRepository.findById(versionId).orElseThrow();
        assertThat(reloadedVersion.getReport().getId()).isEqualTo(report.getId());
        assertThat(reloadedVersion.getVersionNo()).isEqualTo(1);
        assertThat(reloadedVersion.getTasksPlannedNext()).isEqualTo("Finish the API integration");
        assertThat(reloadedVersion.getNotes()).isEqualTo("On track");
        assertThat(reloadedVersion.getLinks()).isEqualTo("https://example.com/board");
        assertThat(reloadedVersion.getSubmittedAt()).isNotNull();

        TaskEntry reloadedTaskEntry = taskEntryRepository.findById(taskEntryId).orElseThrow();
        assertThat(reloadedTaskEntry.getReportVersionId()).isEqualTo(versionId);
        assertThat(reloadedTaskEntry.getTaskName()).isEqualTo("Implement CORS config");
        assertThat(reloadedTaskEntry.getPriority()).isEqualTo("HIGH");
        assertThat(reloadedTaskEntry.getPlannedPct()).isEqualTo(100);
        assertThat(reloadedTaskEntry.getActualPct()).isEqualTo(80);
        assertThat(reloadedTaskEntry.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(reloadedTaskEntry.getTimePlanned()).isEqualTo(8);
        assertThat(reloadedTaskEntry.getTimeSpent()).isEqualTo(6);
        assertThat(reloadedTaskEntry.getDeliverable()).isEqualTo("Working preflight response");

        Blocker reloadedBlocker = blockerRepository.findById(blockerId).orElseThrow();
        assertThat(reloadedBlocker.getReportVersionId()).isEqualTo(versionId);
        assertThat(reloadedBlocker.getDescription()).isEqualTo("Waiting on staging credentials");
        assertThat(reloadedBlocker.isKeyIssue()).isTrue();

        Achievement reloadedAchievement = achievementRepository.findById(achievementId).orElseThrow();
        assertThat(reloadedAchievement.getReportVersionId()).isEqualTo(versionId);
        assertThat(reloadedAchievement.getDescription()).isEqualTo("Shipped the CORS fix");
        assertThat(reloadedAchievement.isKeyHighlight()).isTrue();

        HoursBreakdown reloadedHoursBreakdown =
                hoursBreakdownRepository.findById(hoursBreakdownId).orElseThrow();
        assertThat(reloadedHoursBreakdown.getReportVersionId()).isEqualTo(versionId);
        assertThat(reloadedHoursBreakdown.getTaskType()).isEqualTo("DEVELOPMENT");
        assertThat(reloadedHoursBreakdown.getHours()).isEqualByComparingTo("6.5");
    }

    @Test
    void persistsAndReloadsTheNativeReportStatusEnum() {
        Report report = saveReport(saveUser(), saveProject());
        report.setStatus(ReportStatus.NEEDS_CORRECTION);
        Long reportId = reportRepository.save(report).getId();

        Report reloaded = reportRepository.findById(reportId).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ReportStatus.NEEDS_CORRECTION);
    }

    @Test
    void defaultsReportStatusToDraftAndCurrentVersionNoToZero() {
        Report report = saveReport(saveUser(), saveProject());

        Report reloaded = reportRepository.findById(report.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ReportStatus.DRAFT);
        assertThat(reloaded.getCurrentVersionNo()).isZero();
    }

    @Test
    void rejectsASecondKeyIssueBlockerForTheSameReportVersion() {
        ReportVersion version = saveReportVersion();
        blockerRepository.saveAndFlush(keyIssueBlocker(version.getId()));

        assertThatThrownBy(() -> blockerRepository.saveAndFlush(keyIssueBlocker(version.getId())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsASecondKeyHighlightAchievementForTheSameReportVersion() {
        ReportVersion version = saveReportVersion();
        achievementRepository.saveAndFlush(keyHighlightAchievement(version.getId()));

        assertThatThrownBy(
                        () -> achievementRepository.saveAndFlush(keyHighlightAchievement(version.getId())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private ReportVersion saveReportVersion() {
        Report report = saveReport(saveUser(), saveProject());
        ReportVersion version = new ReportVersion();
        version.setReport(report);
        version.setVersionNo(1);
        return reportVersionRepository.save(version);
    }

    private Blocker keyIssueBlocker(Long versionId) {
        Blocker blocker = new Blocker();
        blocker.setReportVersionId(versionId);
        blocker.setDescription("Something blocking");
        blocker.setKeyIssue(true);
        return blocker;
    }

    private Achievement keyHighlightAchievement(Long versionId) {
        Achievement achievement = new Achievement();
        achievement.setReportVersionId(versionId);
        achievement.setDescription("Something noteworthy");
        achievement.setKeyHighlight(true);
        return achievement;
    }

    private Report saveReport(User user, Project project) {
        Report report = new Report();
        report.setUser(user);
        report.setProject(project);
        report.setWeekStart(LocalDate.of(2026, 9, 1));
        report.setWeekEnd(LocalDate.of(2026, 9, 5));
        return reportRepository.save(report);
    }

    private User saveUser() {
        User user = new User();
        user.setName("Ada Lovelace");
        user.setEmail("ada+" + System.nanoTime() + "@example.com");
        user.setPasswordHash("not-a-real-hash");
        user.setRole(Role.MEMBER);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private Project saveProject() {
        Project project = new Project();
        project.setName("Weekly Report Generator");
        return projectRepository.save(project);
    }
}
