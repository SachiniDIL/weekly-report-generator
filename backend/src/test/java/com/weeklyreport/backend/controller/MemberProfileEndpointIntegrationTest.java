package com.weeklyreport.backend.controller;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
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
class MemberProfileEndpointIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";
    private static final LocalDate WEEK_START = LocalDate.of(2026, 6, 1);
    private static final LocalDate WEEK_END = LocalDate.of(2026, 6, 7);

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

    private User manager;
    private User member;
    private Project project;

    @BeforeEach
    void setUp() {
        deleteAllInFkOrder();
        manager = persistUser("Manager", "manager@example.com", UserStatus.ACTIVE, Role.MANAGER);
        member = persistUser("Mia Member", "mia@example.com", UserStatus.ACTIVE, Role.MEMBER);
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
    void returnsTheMembersOwnStatsScopedToThatMember() throws Exception {
        // Mia: 2 submitted + 1 approved + 1 needs-correction + 1 draft
        long miaSubmitted = currentVersion(persistReport(member, ReportStatus.SUBMITTED));
        persistReport(member, ReportStatus.SUBMITTED);
        persistReport(member, ReportStatus.APPROVED);
        Report needsCorrection = persistReport(member, ReportStatus.NEEDS_CORRECTION);
        persistReport(member, ReportStatus.DRAFT);

        needsCorrection.setCurrentVersionNo(2);
        reportRepository.save(needsCorrection);
        long staleVersion = persistVersion(needsCorrection, 1).getId();
        long currentOfNeedsCorrection = persistVersion(needsCorrection, 2).getId();

        persistHours(miaSubmitted, "DEV", "4.0");
        persistHours(miaSubmitted, "REVIEW", "1.5");
        persistHours(staleVersion, "DEV", "99.0"); // stale version — ignored
        persistHours(currentOfNeedsCorrection, "DEV", "2.0");
        persistHours(currentOfNeedsCorrection, "MEETINGS", "3.0");

        // A different member's data must not leak into Mia's stats.
        User otherMember = persistUser("Other", "other@example.com", UserStatus.ACTIVE, Role.MEMBER);
        persistReport(otherMember, ReportStatus.SUBMITTED);
        persistReport(otherMember, ReportStatus.NEEDS_CORRECTION);
        long otherVersion = currentVersion(persistReport(otherMember, ReportStatus.SUBMITTED));
        persistHours(otherVersion, "DEV", "50.0");

        mockMvc.perform(get("/dashboard/team/" + member.getId()).header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.name").value("Mia Member"))
                .andExpect(jsonPath("$.email").value("mia@example.com"))
                .andExpect(jsonPath("$.totalReportsSubmitted").value(4))
                .andExpect(jsonPath("$.needsCorrectionCount").value(1))
                .andExpect(jsonPath("$.hoursByTaskType", hasSize(3)))
                .andExpect(jsonPath("$.hoursByTaskType[0].taskType").value("DEV"))
                .andExpect(jsonPath("$.hoursByTaskType[0].totalHours").value(closeTo(6.0, 1e-9)))
                .andExpect(jsonPath("$.hoursByTaskType[1].taskType").value("MEETINGS"))
                .andExpect(jsonPath("$.hoursByTaskType[1].totalHours").value(closeTo(3.0, 1e-9)))
                .andExpect(jsonPath("$.hoursByTaskType[2].taskType").value("REVIEW"))
                .andExpect(jsonPath("$.hoursByTaskType[2].totalHours").value(closeTo(1.5, 1e-9)));
    }

    @Test
    void returns404ForAnUnknownUser() throws Exception {
        mockMvc.perform(get("/dashboard/team/999999").header("Authorization", bearer(manager)))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns404WhenTheUserIsNotAMember() throws Exception {
        User anotherManager = persistUser("Boss", "boss@example.com", UserStatus.ACTIVE, Role.MANAGER);
        User admin = persistUser("Admin", "admin@example.com", UserStatus.ACTIVE, Role.ADMIN);

        mockMvc.perform(get("/dashboard/team/" + anotherManager.getId()).header("Authorization", bearer(manager)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/dashboard/team/" + admin.getId()).header("Authorization", bearer(manager)))
                .andExpect(status().isNotFound());
    }

    @Test
    void returns403ForANonManagerCaller() throws Exception {
        mockMvc.perform(get("/dashboard/team/" + member.getId()).header("Authorization", bearer(member)))
                .andExpect(status().isForbidden());
    }

    private long currentVersion(Report report) {
        report.setCurrentVersionNo(1);
        reportRepository.save(report);
        return persistVersion(report, 1).getId();
    }

    private Report persistReport(User user, ReportStatus reportStatus) {
        Report report = new Report();
        report.setUser(user);
        report.setProject(project);
        report.setStatus(reportStatus);
        report.setWeekStart(WEEK_START);
        report.setWeekEnd(WEEK_END);
        return reportRepository.save(report);
    }

    private ReportVersion persistVersion(Report report, int versionNo) {
        ReportVersion version = new ReportVersion();
        version.setReport(report);
        version.setVersionNo(versionNo);
        return reportVersionRepository.save(version);
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

    private Project persistProject() {
        Project newProject = new Project();
        newProject.setName("Apollo");
        return projectRepository.save(newProject);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
