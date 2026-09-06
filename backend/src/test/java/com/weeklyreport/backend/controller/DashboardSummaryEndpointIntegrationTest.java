package com.weeklyreport.backend.controller;

import static org.hamcrest.Matchers.closeTo;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Blocker;
import com.weeklyreport.backend.domain.Project;
import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.ReportStatus;
import com.weeklyreport.backend.domain.ReportVersion;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.repository.AchievementRepository;
import com.weeklyreport.backend.repository.BlockerRepository;
import com.weeklyreport.backend.repository.HoursBreakdownRepository;
import com.weeklyreport.backend.repository.ProjectRepository;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.ReportVersionRepository;
import com.weeklyreport.backend.repository.ReviewCommentRepository;
import com.weeklyreport.backend.repository.TaskEntryRepository;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.security.JwtService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class DashboardSummaryEndpointIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";

    // "Today" is 2026-06-15 (see the mocked clock). This week fully brackets it; last week has
    // already ended; next week hasn't started.
    private static final LocalDate THIS_WEEK_START = LocalDate.of(2026, 6, 14);
    private static final LocalDate THIS_WEEK_END = LocalDate.of(2026, 6, 20);
    private static final LocalDate LAST_WEEK_START = LocalDate.of(2026, 6, 7);
    private static final LocalDate LAST_WEEK_END = LocalDate.of(2026, 6, 13);
    private static final LocalDate NEXT_WEEK_START = LocalDate.of(2026, 6, 21);
    private static final LocalDate NEXT_WEEK_END = LocalDate.of(2026, 6, 27);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportVersionRepository reportVersionRepository;

    @Autowired
    private BlockerRepository blockerRepository;

    @Autowired
    private TaskEntryRepository taskEntryRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private HoursBreakdownRepository hoursBreakdownRepository;

    @Autowired
    private ReviewCommentRepository reviewCommentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private Clock clock;

    private User manager;
    private User member;
    private Project project;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-06-15T09:00:00Z"));
        deleteAllInFkOrder();
        manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        member = persistUser("Member", "member@example.com", Role.MEMBER);
        project = persistProject();
    }

    @AfterEach
    void tearDown() {
        deleteAllInFkOrder();
    }

    private void deleteAllInFkOrder() {
        reviewCommentRepository.deleteAll();
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
    void computesEveryMetricAgainstAKnownMixOfReports() throws Exception {
        // This week (brackets TODAY)
        Report submittedThisWeek = persistReport(ReportStatus.SUBMITTED, THIS_WEEK_START, THIS_WEEK_END);
        Report approvedThisWeek = persistReport(ReportStatus.APPROVED, THIS_WEEK_START, THIS_WEEK_END);
        persistReport(ReportStatus.DRAFT, THIS_WEEK_START, THIS_WEEK_END); // pending — week not over
        Report needsCorrectionThisWeek =
                persistReport(ReportStatus.NEEDS_CORRECTION, THIS_WEEK_START, THIS_WEEK_END);

        // Last week (already ended)
        persistReport(ReportStatus.DRAFT, LAST_WEEK_START, LAST_WEEK_END); // late
        persistReport(ReportStatus.SUBMITTED, LAST_WEEK_START, LAST_WEEK_END);
        persistReport(ReportStatus.NEEDS_CORRECTION, LAST_WEEK_START, LAST_WEEK_END);

        // Next week (not started) — never counted
        persistReport(ReportStatus.DRAFT, NEXT_WEEK_START, NEXT_WEEK_END);

        // Blockers: current-version only, and only on not-yet-APPROVED reports.
        addCurrentVersionWithBlockers(submittedThisWeek, 2);
        addCurrentVersionWithBlockers(approvedThisWeek, 1); // excluded: report is APPROVED
        addCorrectedVersionWithBlockers(needsCorrectionThisWeek); // 1 stale + 1 current -> counts 1

        mockMvc.perform(get("/dashboard/summary").header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSubmittedThisWeek").value(3))
                .andExpect(jsonPath("$.needsCorrectionCount").value(2))
                .andExpect(jsonPath("$.openBlockersCount").value(3))
                // submitted = 5 (SUBMITTED/APPROVED/NEEDS_CORRECTION this week + SUBMITTED/NEEDS_CORRECTION last week)
                // late = 1 (DRAFT last week); rate = 5 / 6
                .andExpect(jsonPath("$.complianceRate").value(closeTo(5.0 / 6.0, 1e-9)));
    }

    @Test
    void reportsFullComplianceWhenNothingIsDueYet() throws Exception {
        persistReport(ReportStatus.DRAFT, THIS_WEEK_START, THIS_WEEK_END);
        persistReport(ReportStatus.DRAFT, NEXT_WEEK_START, NEXT_WEEK_END);

        mockMvc.perform(get("/dashboard/summary").header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complianceRate").value(1.0))
                .andExpect(jsonPath("$.totalSubmittedThisWeek").value(0))
                .andExpect(jsonPath("$.needsCorrectionCount").value(0))
                .andExpect(jsonPath("$.openBlockersCount").value(0));
    }

    @Test
    void isForbiddenForNonManagers() throws Exception {
        mockMvc.perform(get("/dashboard/summary").header("Authorization", bearer(member)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/dashboard/summary")
                        .header("Authorization", bearer(persistUser("Admin", "admin@example.com", Role.ADMIN))))
                .andExpect(status().isForbidden());
    }

    private void addCurrentVersionWithBlockers(Report report, int blockerCount) {
        report.setCurrentVersionNo(1);
        reportRepository.save(report);
        ReportVersion version = persistVersion(report, 1);
        for (int i = 0; i < blockerCount; i++) {
            persistBlocker(version.getId());
        }
    }

    private void addCorrectedVersionWithBlockers(Report report) {
        report.setCurrentVersionNo(2);
        reportRepository.save(report);
        ReportVersion stale = persistVersion(report, 1);
        ReportVersion current = persistVersion(report, 2);
        persistBlocker(stale.getId());
        persistBlocker(current.getId());
    }

    private Report persistReport(ReportStatus status, LocalDate weekStart, LocalDate weekEnd) {
        Report report = new Report();
        report.setUser(member);
        report.setProject(project);
        report.setStatus(status);
        report.setWeekStart(weekStart);
        report.setWeekEnd(weekEnd);
        return reportRepository.save(report);
    }

    private ReportVersion persistVersion(Report report, int versionNo) {
        ReportVersion version = new ReportVersion();
        version.setReport(report);
        version.setVersionNo(versionNo);
        return reportVersionRepository.save(version);
    }

    private void persistBlocker(long reportVersionId) {
        Blocker blocker = new Blocker();
        blocker.setReportVersionId(reportVersionId);
        blocker.setDescription("Something blocking");
        blocker.setKeyIssue(false);
        blockerRepository.save(blocker);
    }

    private User persistUser(String name, String email, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Project persistProject() {
        Project newProject = new Project();
        newProject.setName("Weekly Report Generator");
        return projectRepository.save(newProject);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
