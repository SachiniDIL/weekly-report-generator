package com.weeklyreport.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.weeklyreport.backend.service.GeminiService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AiEndpointsIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";
    private static final LocalDate WEEK_START = LocalDate.of(2026, 6, 1);
    private static final LocalDate WEEK_END = LocalDate.of(2026, 6, 7);
    private static final String SEEDED_TASK = "Migrate billing service to Postgres";
    private static final String AI_ANSWER = "AI answer";

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
    private GeminiService geminiService;

    private User manager;
    private User member;

    @BeforeEach
    void setUp() {
        deleteAllInFkOrder();
        manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        member = persistUser("Priya Member", "priya@example.com", Role.MEMBER);
        seedReportWithContent(member, persistProject());
        lenient().when(geminiService.generate(anyString())).thenReturn(AI_ANSWER);
    }

    @AfterEach
    void tearDown() {
        deleteAllInFkOrder();
    }

    @Test
    void memberIsForbiddenFromBothEndpoints() throws Exception {
        mockMvc.perform(post("/ai/chat")
                        .header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"what happened this week?\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/ai/summary").header("Authorization", bearer(member)))
                .andExpect(status().isForbidden());
    }

    @Test
    void chatSendsGeminiAPromptContainingTheSeededReportData() throws Exception {
        mockMvc.perform(post("/ai/chat")
                        .header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"question":"What is Priya working on?","weekStart":"2026-06-01","weekEnd":"2026-06-07"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(AI_ANSWER));

        String prompt = capturedPrompt();
        assertThat(prompt)
                .contains("Priya Member")
                .contains(SEEDED_TASK)
                .contains("Waiting on infra to provision the replica")
                .contains("What is Priya working on?");
    }

    @Test
    void summarySendsGeminiAPromptContainingTheSeededReportData() throws Exception {
        mockMvc.perform(get("/ai/summary")
                        .param("weekStart", "2026-06-01")
                        .param("weekEnd", "2026-06-07")
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(AI_ANSWER));

        assertThat(capturedPrompt()).contains("Priya Member").contains(SEEDED_TASK);
    }

    private String capturedPrompt() {
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(geminiService).generate(promptCaptor.capture());
        String prompt = promptCaptor.getValue();
        assertThat(prompt).isNotBlank();
        return prompt;
    }

    private void seedReportWithContent(User owner, Project project) {
        Report report = new Report();
        report.setUser(owner);
        report.setProject(project);
        report.setStatus(ReportStatus.SUBMITTED);
        report.setWeekStart(WEEK_START);
        report.setWeekEnd(WEEK_END);
        report.setCurrentVersionNo(1);
        report = reportRepository.save(report);

        ReportVersion version = new ReportVersion();
        version.setReport(report);
        version.setVersionNo(1);
        version = reportVersionRepository.save(version);
        long versionId = version.getId();

        TaskEntry task = new TaskEntry();
        task.setReportVersionId(versionId);
        task.setTaskName(SEEDED_TASK);
        task.setPriority("HIGH");
        task.setPlannedPct(100);
        task.setActualPct(60);
        task.setStatus("IN_PROGRESS");
        task.setTimeSpent(12);
        taskEntryRepository.save(task);

        Blocker blocker = new Blocker();
        blocker.setReportVersionId(versionId);
        blocker.setDescription("Waiting on infra to provision the replica");
        blocker.setKeyIssue(true);
        blockerRepository.save(blocker);

        Achievement achievement = new Achievement();
        achievement.setReportVersionId(versionId);
        achievement.setDescription("Shipped the schema migration scripts");
        achievementRepository.save(achievement);

        HoursBreakdown hours = new HoursBreakdown();
        hours.setReportVersionId(versionId);
        hours.setTaskType("DEV");
        hours.setHours(new BigDecimal("10.0"));
        hoursBreakdownRepository.save(hours);
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
        project.setName("Apollo");
        return projectRepository.save(project);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
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
}
