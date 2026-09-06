package com.weeklyreport.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Achievement;
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
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SectionComparisonEndpointIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";
    private static final LocalDate WEEK_START = LocalDate.of(2026, 6, 1);
    private static final LocalDate WEEK_END = LocalDate.of(2026, 6, 7);
    private static final LocalDate OTHER_WEEK_START = LocalDate.of(2026, 5, 25);
    private static final LocalDate OTHER_WEEK_END = LocalDate.of(2026, 5, 31);

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
    private AchievementRepository achievementRepository;

    @Autowired
    private TaskEntryRepository taskEntryRepository;

    @Autowired
    private HoursBreakdownRepository hoursBreakdownRepository;

    @Autowired
    private ReviewCommentRepository reviewCommentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User manager;
    private User alice;
    private User bob;
    private User carol;
    private User dave;
    private Project project;

    @BeforeEach
    void setUp() {
        deleteAllInFkOrder();
        manager = persistUser("Manager", "manager@example.com", UserStatus.ACTIVE, Role.MANAGER);
        alice = persistUser("Alice", "alice@example.com", UserStatus.ACTIVE, Role.MEMBER);
        bob = persistUser("Bob", "bob@example.com", UserStatus.ACTIVE, Role.MEMBER);
        carol = persistUser("Carol", "carol@example.com", UserStatus.ACTIVE, Role.MEMBER);
        dave = persistUser("Dave", "dave@example.com", UserStatus.ACTIVE, Role.MEMBER);
        persistUser("Pat", "pat@example.com", UserStatus.PENDING, null); // not active — never listed
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
    void showsBlockersOnCurrentVersionsForTheWholeTeamIncludingMembersWithNothingToShow()
            throws Exception {
        Seeded seeded = seedTeamReports();
        persistBlocker(seeded.aliceCurrent(), "Blocked on API", true);
        persistBlocker(seeded.aliceCurrent(), "Waiting on design", false);
        persistBlocker(seeded.bobStale(), "Old blocker", false); // stale version — excluded
        persistBlocker(seeded.bobCurrent(), "Env access", false);
        persistBlocker(seeded.carolDraft(), "Draft blocker", false); // DRAFT report — excluded

        mockMvc.perform(get("/dashboard/section")
                        .param("section", "blockers")
                        .param("weekStart", "2026-06-01")
                        .param("weekEnd", "2026-06-07")
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].memberName").value("Alice"))
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"))
                .andExpect(jsonPath("$[0].items", hasSize(2)))
                .andExpect(jsonPath("$[0].items[0].description").value("Blocked on API"))
                .andExpect(jsonPath("$[0].items[0].key").value(true))
                .andExpect(jsonPath("$[0].items[1].description").value("Waiting on design"))
                .andExpect(jsonPath("$[0].items[1].key").value(false))
                .andExpect(jsonPath("$[1].memberName").value("Bob"))
                .andExpect(jsonPath("$[1].status").value("NEEDS_CORRECTION"))
                .andExpect(jsonPath("$[1].items", hasSize(1)))
                .andExpect(jsonPath("$[1].items[0].description").value("Env access"))
                .andExpect(jsonPath("$[2].memberName").value("Carol"))
                .andExpect(jsonPath("$[2].status").value("DRAFT"))
                .andExpect(jsonPath("$[2].items", hasSize(0)))
                .andExpect(jsonPath("$[3].memberName").value("Dave"))
                .andExpect(jsonPath("$[3].status").value("NOT_STARTED"))
                .andExpect(jsonPath("$[3].items", hasSize(0)));
    }

    @Test
    void showsAchievementsWithTheKeyHighlightFlagComingThrough() throws Exception {
        Seeded seeded = seedTeamReports();
        persistAchievement(seeded.aliceCurrent(), "Shipped auth", false);
        persistAchievement(seeded.aliceCurrent(), "Cut the release", true);
        persistAchievement(seeded.bobStale(), "Old win", true); // stale version — excluded
        persistAchievement(seeded.bobCurrent(), "Fixed the build", false);
        persistAchievement(seeded.carolDraft(), "Draft win", false); // DRAFT report — excluded

        mockMvc.perform(get("/dashboard/section")
                        .param("section", "achievements")
                        .param("weekStart", "2026-06-01")
                        .param("weekEnd", "2026-06-07")
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].memberName").value("Alice"))
                .andExpect(jsonPath("$[0].items", hasSize(2)))
                .andExpect(jsonPath("$[0].items[0].description").value("Shipped auth"))
                .andExpect(jsonPath("$[0].items[0].key").value(false))
                .andExpect(jsonPath("$[0].items[1].description").value("Cut the release"))
                .andExpect(jsonPath("$[0].items[1].key").value(true))
                .andExpect(jsonPath("$[1].memberName").value("Bob"))
                .andExpect(jsonPath("$[1].items", hasSize(1)))
                .andExpect(jsonPath("$[1].items[0].description").value("Fixed the build"))
                .andExpect(jsonPath("$[2].memberName").value("Carol"))
                .andExpect(jsonPath("$[2].items", hasSize(0)))
                .andExpect(jsonPath("$[3].memberName").value("Dave"))
                .andExpect(jsonPath("$[3].items", hasSize(0)));
    }

    @Test
    void rejectsAnUnknownSection() throws Exception {
        mockMvc.perform(get("/dashboard/section")
                        .param("section", "nonsense")
                        .param("weekStart", "2026-06-01")
                        .param("weekEnd", "2026-06-07")
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isForbiddenForNonManagers() throws Exception {
        User admin = persistUser("Root", "root@example.com", UserStatus.ACTIVE, Role.ADMIN);

        mockMvc.perform(get("/dashboard/section")
                        .param("section", "blockers")
                        .header("Authorization", bearer(alice)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/dashboard/section")
                        .param("section", "blockers")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isForbidden());
    }

    private Seeded seedTeamReports() {
        long aliceCurrent = currentVersion(persistReport(alice, ReportStatus.SUBMITTED, WEEK_START, WEEK_END));

        Report bobReport = persistReport(bob, ReportStatus.NEEDS_CORRECTION, WEEK_START, WEEK_END);
        bobReport.setCurrentVersionNo(2);
        reportRepository.save(bobReport);
        long bobStale = persistVersion(bobReport, 1).getId();
        long bobCurrent = persistVersion(bobReport, 2).getId();

        long carolDraft = currentVersion(persistReport(carol, ReportStatus.DRAFT, WEEK_START, WEEK_END));

        // Dave: nothing this week (but a report elsewhere, to prove "no report *for the week*").
        currentVersion(persistReport(dave, ReportStatus.SUBMITTED, OTHER_WEEK_START, OTHER_WEEK_END));

        // A non-member and a pending user with content this week — must never appear.
        persistBlocker(
                currentVersion(persistReport(manager, ReportStatus.SUBMITTED, WEEK_START, WEEK_END)),
                "Manager blocker",
                false);

        return new Seeded(aliceCurrent, bobStale, bobCurrent, carolDraft);
    }

    private record Seeded(long aliceCurrent, long bobStale, long bobCurrent, long carolDraft) {}

    private long currentVersion(Report report) {
        report.setCurrentVersionNo(1);
        reportRepository.save(report);
        return persistVersion(report, 1).getId();
    }

    private Report persistReport(
            User user, ReportStatus reportStatus, LocalDate weekStart, LocalDate weekEnd) {
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

    private void persistBlocker(long reportVersionId, String description, boolean keyIssue) {
        Blocker blocker = new Blocker();
        blocker.setReportVersionId(reportVersionId);
        blocker.setDescription(description);
        blocker.setKeyIssue(keyIssue);
        blockerRepository.save(blocker);
    }

    private void persistAchievement(long reportVersionId, String description, boolean keyHighlight) {
        Achievement achievement = new Achievement();
        achievement.setReportVersionId(reportVersionId);
        achievement.setDescription(description);
        achievement.setKeyHighlight(keyHighlight);
        achievementRepository.save(achievement);
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

    private Project persistProject() {
        Project newProject = new Project();
        newProject.setName("Apollo");
        return projectRepository.save(newProject);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
