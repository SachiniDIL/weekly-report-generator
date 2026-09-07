package com.weeklyreport.backend.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.ReportStatus;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.repository.AchievementRepository;
import com.weeklyreport.backend.repository.BlockerRepository;
import com.weeklyreport.backend.repository.HoursBreakdownRepository;
import com.weeklyreport.backend.repository.ProjectAssignmentRepository;
import com.weeklyreport.backend.repository.ProjectRepository;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.ReportVersionRepository;
import com.weeklyreport.backend.repository.ReviewCommentRepository;
import com.weeklyreport.backend.repository.TaskEntryRepository;
import com.weeklyreport.backend.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Runs the app with the {@code seed} profile so {@link ProductionSeeder} executes on startup,
 * then checks the dataset landed and that re-running the seeder changes nothing.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("seed")
class ProductionSeederIntegrationTest {

    @Autowired
    private ProductionSeeder productionSeeder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportVersionRepository reportVersionRepository;

    @Autowired
    private ReviewCommentRepository reviewCommentRepository;

    @Autowired
    private TaskEntryRepository taskEntryRepository;

    @Autowired
    private BlockerRepository blockerRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private HoursBreakdownRepository hoursBreakdownRepository;

    @AfterEach
    void clearSeededData() {
        reviewCommentRepository.deleteAll();
        hoursBreakdownRepository.deleteAll();
        achievementRepository.deleteAll();
        blockerRepository.deleteAll();
        taskEntryRepository.deleteAll();
        reportVersionRepository.deleteAll();
        reportRepository.deleteAll();
        projectAssignmentRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void seedsTheDatasetOnStartupAndIsIdempotentWhenRerun() {
        assertThat(seededUsers(Role.MANAGER)).hasSize(SeedCatalog.MANAGERS.size());
        assertThat(seededUsers(Role.MEMBER)).hasSize(SeedCatalog.MEMBERS.size());
        assertThat(seededUsers(Role.MANAGER))
                .allSatisfy(user -> assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE));

        assertThat(projectRepository.existsByName("Client A")).isTrue();
        assertThat(projectRepository.existsByName("Internal Tooling")).isTrue();
        assertThat(projectRepository.existsByName("R&D")).isTrue();
        assertThat(projectAssignmentRepository.count()).isGreaterThanOrEqualTo(5);

        List<Report> seededReports = seededReports();
        assertThat(seededReports).hasSize(SeedCatalog.reports().size());
        assertThat(seededReports)
                .extracting(Report::getStatus)
                .contains(
                        ReportStatus.DRAFT,
                        ReportStatus.SUBMITTED,
                        ReportStatus.NEEDS_CORRECTION,
                        ReportStatus.APPROVED);

        // The multi-correction report exists: driven to version 3 (two "changes requested"
        // rounds), so its version history actually shows something.
        Report correctedReport = seededReports.stream()
                .filter(report -> report.getCurrentVersionNo() >= 3)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected a report with two correction cycles"));
        assertThat(reportVersionRepository.findByReportIdOrderByVersionNoDesc(correctedReport.getId()))
                .hasSize(3);
        assertThat(reviewCommentRepository.count()).isGreaterThanOrEqualTo(3);

        long usersBefore = userRepository.count();
        long reportsBefore = reportRepository.count();
        long versionsBefore = reportVersionRepository.count();

        productionSeeder.run();

        assertThat(userRepository.count()).isEqualTo(usersBefore);
        assertThat(reportRepository.count()).isEqualTo(reportsBefore);
        assertThat(reportVersionRepository.count()).isEqualTo(versionsBefore);
    }

    private List<User> seededUsers(Role role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getEmail().endsWith("@seed.dev"))
                .filter(user -> user.getRole() == role)
                .toList();
    }

    private List<Report> seededReports() {
        // getId() on the lazy User proxy is safe; navigating other fields would need a session.
        Set<Long> seededUserIds = userRepository.findAll().stream()
                .filter(user -> user.getEmail().endsWith("@seed.dev"))
                .map(User::getId)
                .collect(Collectors.toSet());
        return reportRepository.findAll().stream()
                .filter(report -> seededUserIds.contains(report.getUser().getId()))
                .toList();
    }
}
