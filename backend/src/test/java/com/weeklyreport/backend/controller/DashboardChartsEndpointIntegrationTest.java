package com.weeklyreport.backend.controller;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.HoursBreakdown;
import com.weeklyreport.backend.domain.Project;
import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.ReportStatus;
import com.weeklyreport.backend.domain.ReportVersion;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.TaskEntry;
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
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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
class DashboardChartsEndpointIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";

    // "Today" is 2026-06-17 (see the mocked clock).
    private static final LocalDate W1_START = LocalDate.of(2026, 5, 25);
    private static final LocalDate W1_END = LocalDate.of(2026, 5, 31);
    private static final LocalDate W2_START = LocalDate.of(2026, 6, 1);
    private static final LocalDate W2_END = LocalDate.of(2026, 6, 7);
    private static final LocalDate W3_START = LocalDate.of(2026, 6, 8);
    private static final LocalDate W3_END = LocalDate.of(2026, 6, 14);
    private static final LocalDate FUTURE_START = LocalDate.of(2026, 6, 22);
    private static final LocalDate FUTURE_END = LocalDate.of(2026, 6, 28);

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
    private TaskEntryRepository taskEntryRepository;

    @Autowired
    private BlockerRepository blockerRepository;

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
    private Project apollo;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-06-17T10:00:00Z"));
        deleteAllInFkOrder();
        manager = persistUser("Manager", "manager@example.com", UserStatus.ACTIVE, Role.MANAGER);
        member = persistUser("Member", "member@example.com", UserStatus.ACTIVE, Role.MEMBER);
        apollo = persistProject("Apollo");
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
    void tasksCompletedTrendCountsHundredPercentTasksOnCurrentVersionsPerWeek() throws Exception {
        // W1: two tasks at 100%, one below -> 2
        long w1v1 = currentVersion(persistReport(member, apollo, ReportStatus.SUBMITTED, W1_START, W1_END));
        persistTask(w1v1, 100);
        persistTask(w1v1, 100);
        persistTask(w1v1, 40);

        // W2: a plain report (1) + a corrected report whose stale version's 100% task is ignored
        long w2plain = currentVersion(persistReport(member, apollo, ReportStatus.SUBMITTED, W2_START, W2_END));
        persistTask(w2plain, 100);
        Report corrected = persistReport(member, apollo, ReportStatus.NEEDS_CORRECTION, W2_START, W2_END);
        corrected.setCurrentVersionNo(2);
        reportRepository.save(corrected);
        long staleVersion = persistVersion(corrected, 1).getId();
        long currentOfCorrected = persistVersion(corrected, 2).getId();
        persistTask(staleVersion, 100); // ignored — not the current version
        persistTask(currentOfCorrected, 100);
        persistTask(currentOfCorrected, 100);

        // W3: a report with no task entries at all -> the week still shows, at 0
        currentVersion(persistReport(member, apollo, ReportStatus.DRAFT, W3_START, W3_END));

        // Future week -> never in the trend
        long futureVersion =
                currentVersion(persistReport(member, apollo, ReportStatus.SUBMITTED, FUTURE_START, FUTURE_END));
        persistTask(futureVersion, 100);

        mockMvc.perform(get("/dashboard/charts/tasks-completed-trend").header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].weekStart").value("2026-05-25"))
                .andExpect(jsonPath("$[0].weekEnd").value("2026-05-31"))
                .andExpect(jsonPath("$[0].completedTasks").value(2))
                .andExpect(jsonPath("$[1].weekStart").value("2026-06-01"))
                .andExpect(jsonPath("$[1].completedTasks").value(3))
                .andExpect(jsonPath("$[2].weekStart").value("2026-06-08"))
                .andExpect(jsonPath("$[2].completedTasks").value(0));

        mockMvc.perform(get("/dashboard/charts/tasks-completed-trend")
                        .param("weeks", "2")
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].weekStart").value("2026-06-01"))
                .andExpect(jsonPath("$[0].completedTasks").value(3))
                .andExpect(jsonPath("$[1].completedTasks").value(0));
    }

    @Test
    void submissionStatusByMemberCoversEveryActiveMemberAndRollsUpTheLeastAdvanced() throws Exception {
        Project zephyr = persistProject("Zephyr");
        User alice = persistUser("Alice", "alice@example.com", UserStatus.ACTIVE, Role.MEMBER);
        User bob = persistUser("Bob", "bob@example.com", UserStatus.ACTIVE, Role.MEMBER);
        persistUser("Carol", "carol@example.com", UserStatus.ACTIVE, Role.MEMBER);
        User dana = persistUser("Dana", "dana@example.com", UserStatus.ACTIVE, Role.MEMBER);
        User pending = persistUser("Pat", "pat@example.com", UserStatus.PENDING, null);

        persistReport(alice, apollo, ReportStatus.SUBMITTED, W2_START, W2_END);
        persistReport(alice, apollo, ReportStatus.APPROVED, W1_START, W1_END); // other week — ignored
        persistReport(bob, apollo, ReportStatus.APPROVED, W2_START, W2_END);
        persistReport(bob, zephyr, ReportStatus.NEEDS_CORRECTION, W2_START, W2_END); // least advanced wins
        persistReport(dana, apollo, ReportStatus.DRAFT, W2_START, W2_END);
        persistReport(manager, apollo, ReportStatus.DRAFT, W2_START, W2_END); // manager — not a "member"
        persistReport(pending, apollo, ReportStatus.SUBMITTED, W2_START, W2_END); // pending — not active

        mockMvc.perform(get("/dashboard/charts/submission-status-by-member")
                        .param("weekStart", "2026-06-01")
                        .param("weekEnd", "2026-06-07")
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].memberName").value("Alice"))
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"))
                .andExpect(jsonPath("$[1].memberName").value("Bob"))
                .andExpect(jsonPath("$[1].status").value("NEEDS_CORRECTION"))
                .andExpect(jsonPath("$[2].memberName").value("Carol"))
                .andExpect(jsonPath("$[2].status").value("NOT_STARTED"))
                .andExpect(jsonPath("$[3].memberName").value("Dana"))
                .andExpect(jsonPath("$[3].status").value("DRAFT"))
                .andExpect(jsonPath("$[4].memberName").value("Member"))
                .andExpect(jsonPath("$[4].status").value("NOT_STARTED"));
    }

    @Test
    void workloadByProjectCountsCurrentVersionTaskEntriesGroupedByProject() throws Exception {
        Project zephyr = persistProject("Zephyr");

        long apolloPlain = currentVersion(persistReport(member, apollo, ReportStatus.SUBMITTED, W2_START, W2_END));
        persistTask(apolloPlain, 10);
        persistTask(apolloPlain, 20);
        persistTask(apolloPlain, 30);

        long zephyrPlain = currentVersion(persistReport(member, zephyr, ReportStatus.SUBMITTED, W2_START, W2_END));
        persistTask(zephyrPlain, 10);
        persistTask(zephyrPlain, 10);
        persistTask(zephyrPlain, 10);
        persistTask(zephyrPlain, 10);

        Report corrected = persistReport(member, apollo, ReportStatus.NEEDS_CORRECTION, W2_START, W2_END);
        corrected.setCurrentVersionNo(2);
        reportRepository.save(corrected);
        long stale = persistVersion(corrected, 1).getId();
        long current = persistVersion(corrected, 2).getId();
        persistTask(stale, 0);
        persistTask(stale, 0); // stale version — ignored
        persistTask(current, 0);
        persistTask(current, 0);

        long otherWeek = currentVersion(persistReport(member, apollo, ReportStatus.DRAFT, W1_START, W1_END));
        persistTask(otherWeek, 0); // different week — ignored

        mockMvc.perform(get("/dashboard/charts/workload-by-project")
                        .param("weekStart", "2026-06-01")
                        .param("weekEnd", "2026-06-07")
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].projectName").value("Apollo"))
                .andExpect(jsonPath("$[0].taskCount").value(5))
                .andExpect(jsonPath("$[1].projectName").value("Zephyr"))
                .andExpect(jsonPath("$[1].taskCount").value(4));
    }

    @Test
    void timeByTaskTypeSumsCurrentVersionHoursGroupedByTaskType() throws Exception {
        Project zephyr = persistProject("Zephyr");

        long v1 = currentVersion(persistReport(member, apollo, ReportStatus.SUBMITTED, W2_START, W2_END));
        persistHours(v1, "DEV", "5.0");
        persistHours(v1, "REVIEW", "2.0");

        long v2 = currentVersion(persistReport(member, zephyr, ReportStatus.SUBMITTED, W2_START, W2_END));
        persistHours(v2, "DEV", "3.5");
        persistHours(v2, "MEETINGS", "1.0");

        Report corrected = persistReport(member, apollo, ReportStatus.NEEDS_CORRECTION, W2_START, W2_END);
        corrected.setCurrentVersionNo(2);
        reportRepository.save(corrected);
        long stale = persistVersion(corrected, 1).getId();
        long current = persistVersion(corrected, 2).getId();
        persistHours(stale, "DEV", "100.0"); // stale version — ignored
        persistHours(current, "DEV", "1.0");

        long otherWeek = currentVersion(persistReport(member, apollo, ReportStatus.DRAFT, W1_START, W1_END));
        persistHours(otherWeek, "DEV", "50.0"); // different week — ignored

        mockMvc.perform(get("/dashboard/charts/time-by-task-type")
                        .param("weekStart", "2026-06-01")
                        .param("weekEnd", "2026-06-07")
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].taskType").value("DEV"))
                .andExpect(jsonPath("$[0].totalHours").value(closeTo(9.5, 1e-9)))
                .andExpect(jsonPath("$[1].taskType").value("REVIEW"))
                .andExpect(jsonPath("$[1].totalHours").value(closeTo(2.0, 1e-9)))
                .andExpect(jsonPath("$[2].taskType").value("MEETINGS"));
    }

    @Test
    void chartRangeDefaultsToTheIsoWeekContainingToday() throws Exception {
        LocalDate monday = LocalDate.of(2026, 6, 17).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        long version =
                currentVersion(persistReport(member, apollo, ReportStatus.SUBMITTED, monday, monday.plusDays(6)));
        persistTask(version, 0);
        persistTask(version, 0);

        mockMvc.perform(get("/dashboard/charts/workload-by-project").header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].projectName").value("Apollo"))
                .andExpect(jsonPath("$[0].taskCount").value(2));
    }

    @Test
    void chartEndpointsAreForbiddenForNonManagers() throws Exception {
        mockMvc.perform(get("/dashboard/charts/tasks-completed-trend").header("Authorization", bearer(member)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/dashboard/charts/workload-by-project").header("Authorization", bearer(member)))
                .andExpect(status().isForbidden());
    }

    private long currentVersion(Report report) {
        report.setCurrentVersionNo(1);
        reportRepository.save(report);
        return persistVersion(report, 1).getId();
    }

    private Report persistReport(
            User user, Project project, ReportStatus reportStatus, LocalDate weekStart, LocalDate weekEnd) {
        Report report = new Report();
        report.setUser(user);
        report.setProject(project);
        report.setStatus(reportStatus);
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

    private void persistTask(long reportVersionId, int actualPct) {
        TaskEntry entry = new TaskEntry();
        entry.setReportVersionId(reportVersionId);
        entry.setTaskName("Task");
        entry.setPriority("MEDIUM");
        entry.setPlannedPct(100);
        entry.setActualPct(actualPct);
        entry.setStatus("in progress");
        taskEntryRepository.save(entry);
    }

    private void persistHours(long reportVersionId, String taskType, String hours) {
        HoursBreakdown breakdown = new HoursBreakdown();
        breakdown.setReportVersionId(reportVersionId);
        breakdown.setTaskType(taskType);
        breakdown.setHours(new BigDecimal(hours));
        hoursBreakdownRepository.save(breakdown);
    }

    private User persistUser(String name, String email, UserStatus userStatus, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setStatus(userStatus);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Project persistProject(String name) {
        Project project = new Project();
        project.setName(name);
        return projectRepository.save(project);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
