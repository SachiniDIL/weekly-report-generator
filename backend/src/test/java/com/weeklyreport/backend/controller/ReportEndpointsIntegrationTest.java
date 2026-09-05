package com.weeklyreport.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.weeklyreport.backend.TestcontainersConfiguration;
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
class ReportEndpointsIntegrationTest {

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

    // Reports created here would otherwise survive into ProjectRepositoryTest / UserRepositoryTest
    // and break their own deleteAll() via FK, same fix as ReportRepositoryTest's own @AfterEach.
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
    void walksThroughCreateEditAndSubmit() throws Exception {
        User owner = persistUser("Member", "member@example.com", Role.MEMBER);
        Project project = persistProject();

        String createBody = """
                {"projectId": %d, "weekStart": "2026-09-01", "weekEnd": "2026-09-05",
                 "content": {"tasksPlannedNext": "Start the API", "notes": "Kickoff week",
                 "links": "https://example.com/board",
                 "taskEntries": [{"taskName": "Design schema", "priority": "HIGH",
                     "plannedPct": 100, "actualPct": 50, "status": "IN_PROGRESS",
                     "timePlanned": 8, "timeSpent": 4, "deliverable": "ERD"}],
                 "blockers": [{"description": "Waiting on access", "isKeyIssue": false}],
                 "achievements": [{"description": "Kicked off", "isKeyHighlight": false}],
                 "hoursBreakdown": [{"taskType": "PLANNING", "hours": 3.5}]}}
                """
                .formatted(project.getId());

        String createResponse = mockMvc.perform(post("/reports")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.currentVersionNo").value(1))
                .andExpect(jsonPath("$.content.taskEntries", hasSize(1)))
                .andExpect(jsonPath("$.content.submittedAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long reportId = ((Number) JsonPath.read(createResponse, "$.id")).longValue();

        String editBody = """
                {"tasksPlannedNext": "Finish the API", "notes": "On track now", "links": null,
                 "taskEntries": [], "blockers": [{"description": "Unblocked", "isKeyIssue": true}],
                 "achievements": [], "hoursBreakdown": []}
                """;

        mockMvc.perform(put("/reports/" + reportId)
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.notes").value("On track now"))
                .andExpect(jsonPath("$.content.taskEntries", hasSize(0)))
                .andExpect(jsonPath("$.content.blockers", hasSize(1)))
                .andExpect(jsonPath("$.content.blockers[0].isKeyIssue").value(true));

        mockMvc.perform(post("/reports/" + reportId + "/submit").header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.currentVersionNo").value(1));

        Report reloadedReport = reportRepository.findById(reportId).orElseThrow();
        assertThat(reloadedReport.getStatus()).isEqualTo(ReportStatus.SUBMITTED);
        assertThat(reloadedReport.getCurrentVersionNo()).isEqualTo(1);

        ReportVersion reloadedVersion =
                reportVersionRepository.findByReportIdAndVersionNo(reportId, 1).orElseThrow();
        assertThat(reloadedVersion.getSubmittedAt()).isNotNull();
        assertThat(reloadedVersion.getNotes()).isEqualTo("On track now");
    }

    @Test
    void aUserCannotEditOrSubmitAnotherUsersReport() throws Exception {
        User owner = persistUser("Owner", "owner@example.com", Role.MEMBER);
        User intruder = persistUser("Intruder", "intruder@example.com", Role.MEMBER);
        long reportId = createDraftReport(owner);

        mockMvc.perform(put("/reports/" + reportId)
                        .header("Authorization", bearer(intruder))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyContentBody()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/reports/" + reportId + "/submit").header("Authorization", bearer(intruder)))
                .andExpect(status().isForbidden());
    }

    @Test
    void aManagerCannotEditOrSubmitAReportTheyDoNotOwn() throws Exception {
        // A manager can review and comment on this report, but must never be able to rewrite its
        // content — the ownership check applies to a manager exactly as to any other non-owner.
        User owner = persistUser("Owner", "owner@example.com", Role.MEMBER);
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        long reportId = createDraftReport(owner);

        mockMvc.perform(put("/reports/" + reportId)
                        .header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyContentBody()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/reports/" + reportId + "/submit").header("Authorization", bearer(manager)))
                .andExpect(status().isForbidden());
    }

    @Test
    void editingOrSubmittingANonEditableReportIsRejected() throws Exception {
        User owner = persistUser("Member", "member@example.com", Role.MEMBER);
        long reportId = createDraftReport(owner);

        mockMvc.perform(post("/reports/" + reportId + "/submit").header("Authorization", bearer(owner)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/reports/" + reportId)
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyContentBody()))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/reports/" + reportId + "/submit").header("Authorization", bearer(owner)))
                .andExpect(status().isConflict());
    }

    @Test
    void multipleKeyIssueBlockersProduceACleanValidationErrorNotARawDatabaseException() throws Exception {
        User owner = persistUser("Member", "member@example.com", Role.MEMBER);
        Project project = persistProject();

        String createBody = """
                {"projectId": %d, "weekStart": "2026-09-01", "weekEnd": "2026-09-05",
                 "content": {"blockers": [
                     {"description": "First blocker", "isKeyIssue": true},
                     {"description": "Second blocker", "isKeyIssue": true}]}}
                """
                .formatted(project.getId());

        mockMvc.perform(post("/reports")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    private long createDraftReport(User owner) throws Exception {
        Project project = persistProject();
        String createBody = """
                {"projectId": %d, "weekStart": "2026-09-01", "weekEnd": "2026-09-05"}
                """
                .formatted(project.getId());

        String response = mockMvc.perform(post("/reports")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
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

    private static String emptyContentBody() {
        return """
                {"taskEntries": [], "blockers": [], "achievements": [], "hoursBreakdown": []}
                """;
    }
}
