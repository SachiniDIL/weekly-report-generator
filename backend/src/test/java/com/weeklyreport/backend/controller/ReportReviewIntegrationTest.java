package com.weeklyreport.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Project;
import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.ReportStatus;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ReportReviewIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";

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
    private ReviewCommentRepository reviewCommentRepository;

    @Autowired
    private TaskEntryRepository taskEntryRepository;

    @Autowired
    private BlockerRepository blockerRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private HoursBreakdownRepository hoursBreakdownRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void clearTables() {
        deleteAllInFkOrder();
    }

    @AfterEach
    void cleanUpAfterSelf() {
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
    void walksThroughTheFullCorrectionCycle() throws Exception {
        User member = persistUser("Member", "member@example.com", Role.MEMBER);
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        Project project = persistProject();

        long reportId = createReportWithOneTaskEntry(member, project.getId(), "Design the schema");
        submit(member, reportId);

        String requestChangesBody = """
                {"action": "CHANGES_REQUESTED", "comment": "Please add hours breakdown"}
                """;
        mockMvc.perform(post("/reports/" + reportId + "/review")
                        .header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestChangesBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEEDS_CORRECTION"))
                .andExpect(jsonPath("$.currentVersionNo").value(2))
                .andExpect(jsonPath("$.content.versionNo").value(2))
                .andExpect(jsonPath("$.content.taskEntries", hasSize(1)))
                .andExpect(jsonPath("$.content.taskEntries[0].taskName").value("Design the schema"));

        Report afterReview = reportRepository.findById(reportId).orElseThrow();
        assertThat(afterReview.getStatus()).isEqualTo(ReportStatus.NEEDS_CORRECTION);
        assertThat(afterReview.getCurrentVersionNo()).isEqualTo(2);

        String editBody = """
                {"taskEntries": [{"taskName": "Design the schema", "priority": "HIGH",
                    "plannedPct": 100, "actualPct": 100, "status": "DONE"}],
                 "hoursBreakdown": [{"taskType": "DEVELOPMENT", "hours": 4}]}
                """;
        mockMvc.perform(put("/reports/" + reportId)
                        .header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.hoursBreakdown", hasSize(1)));

        mockMvc.perform(post("/reports/" + reportId + "/submit").header("Authorization", bearer(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.currentVersionNo").value(2));

        String approveBody = """
                {"action": "APPROVED"}
                """;
        mockMvc.perform(post("/reports/" + reportId + "/review")
                        .header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approveBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.currentVersionNo").value(2));

        Report finalReport = reportRepository.findById(reportId).orElseThrow();
        assertThat(finalReport.getStatus()).isEqualTo(ReportStatus.APPROVED);
        assertThat(finalReport.getCurrentVersionNo()).isEqualTo(2);

        mockMvc.perform(get("/reports/" + reportId + "/versions").header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content.versionNo").value(2))
                .andExpect(jsonPath("$[0].reviewComment.action").value("APPROVED"))
                .andExpect(jsonPath("$[0].content.hoursBreakdown", hasSize(1)))
                .andExpect(jsonPath("$[1].content.versionNo").value(1))
                .andExpect(jsonPath("$[1].reviewComment.action").value("CHANGES_REQUESTED"))
                .andExpect(jsonPath("$[1].reviewComment.comment").value("Please add hours breakdown"))
                .andExpect(jsonPath("$[1].reviewComment.managerName").value("Manager"))
                .andExpect(jsonPath("$[1].content.hoursBreakdown", hasSize(0)));
    }

    @Test
    void reviewingANonSubmittedReportIsRejected() throws Exception {
        User member = persistUser("Member", "member@example.com", Role.MEMBER);
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        Project project = persistProject();
        long draftReportId = createReportWithOneTaskEntry(member, project.getId(), "Task");

        mockMvc.perform(post("/reports/" + draftReportId + "/review")
                        .header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"action": "APPROVED"}
                                """))
                .andExpect(status().isConflict());

        submit(member, draftReportId);
        mockMvc.perform(post("/reports/" + draftReportId + "/review")
                        .header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"action": "APPROVED"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/reports/" + draftReportId + "/review")
                        .header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"action": "APPROVED"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void requestingChangesWithoutACommentIsRejected() throws Exception {
        User member = persistUser("Member", "member@example.com", Role.MEMBER);
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        Project project = persistProject();
        long reportId = createReportWithOneTaskEntry(member, project.getId(), "Task");
        submit(member, reportId);

        mockMvc.perform(post("/reports/" + reportId + "/review")
                        .header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"action": "CHANGES_REQUESTED"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aNonManagerIsForbiddenFromTheReviewEndpoint() throws Exception {
        User member = persistUser("Member", "member@example.com", Role.MEMBER);
        User admin = persistUser("Admin", "admin@example.com", Role.ADMIN);
        User anotherMember = persistUser("Another Member", "another@example.com", Role.MEMBER);
        Project project = persistProject();
        long reportId = createReportWithOneTaskEntry(member, project.getId(), "Task");
        submit(member, reportId);

        String approveBody = """
                {"action": "APPROVED"}
                """;
        mockMvc.perform(post("/reports/" + reportId + "/review")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approveBody))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/reports/" + reportId + "/review")
                        .header("Authorization", bearer(anotherMember))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approveBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void versionHistoryRespectsTheSameVisibilityRuleAsReportDetail() throws Exception {
        User owner = persistUser("Owner", "owner@example.com", Role.MEMBER);
        User otherMember = persistUser("Other Member", "other@example.com", Role.MEMBER);
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        User admin = persistUser("Admin", "admin@example.com", Role.ADMIN);
        Project project = persistProject();
        long reportId = createReportWithOneTaskEntry(owner, project.getId(), "Task");

        mockMvc.perform(get("/reports/" + reportId + "/versions").header("Authorization", bearer(owner)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/reports/" + reportId + "/versions").header("Authorization", bearer(manager)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/reports/" + reportId + "/versions").header("Authorization", bearer(otherMember)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/reports/" + reportId + "/versions").header("Authorization", bearer(admin)))
                .andExpect(status().isForbidden());
    }

    private void submit(User member, long reportId) throws Exception {
        mockMvc.perform(post("/reports/" + reportId + "/submit").header("Authorization", bearer(member)))
                .andExpect(status().isOk());
    }

    private long createReportWithOneTaskEntry(User owner, Long projectId, String taskName) throws Exception {
        String body = """
                {"projectId": %d, "weekStart": "2026-09-01", "weekEnd": "2026-09-05",
                 "content": {"taskEntries": [{"taskName": "%s", "priority": "HIGH",
                     "plannedPct": 100, "actualPct": 50, "status": "IN_PROGRESS"}]}}
                """
                .formatted(projectId, taskName);

        String response = mockMvc.perform(post("/reports")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
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
        Project project = new Project();
        project.setName("Weekly Report Generator");
        return projectRepository.save(project);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
