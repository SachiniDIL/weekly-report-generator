package com.weeklyreport.backend.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Project;
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
class ReportListingIntegrationTest {

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
    void aMembersListOnlyEverContainsTheirOwnReportsEvenWithOtherFiltersSupplied() throws Exception {
        User memberA = persistUser("Member A", "membera@example.com", Role.MEMBER);
        User memberB = persistUser("Member B", "memberb@example.com", Role.MEMBER);
        Project project = persistProject();
        createReport(memberA, project.getId(), "2026-09-01", "2026-09-05");
        createReport(memberB, project.getId(), "2026-09-01", "2026-09-05");

        mockMvc.perform(get("/reports")
                        .header("Authorization", bearer(memberA))
                        .param("projectId", project.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].ownerName").value("Member A"));
    }

    @Test
    void aMemberExplicitlyRequestingAnotherMembersReportsIsRejected() throws Exception {
        User memberA = persistUser("Member A", "membera@example.com", Role.MEMBER);
        User memberB = persistUser("Member B", "memberb@example.com", Role.MEMBER);

        mockMvc.perform(get("/reports")
                        .header("Authorization", bearer(memberA))
                        .param("userId", memberB.getId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void aManagersListSeesAcrossMembersAndSupportsFilteringByOneMember() throws Exception {
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        User memberA = persistUser("Member A", "membera@example.com", Role.MEMBER);
        User memberB = persistUser("Member B", "memberb@example.com", Role.MEMBER);
        Project project = persistProject();
        createReport(memberA, project.getId(), "2026-09-01", "2026-09-05");
        createReport(memberB, project.getId(), "2026-09-01", "2026-09-05");

        mockMvc.perform(get("/reports").header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].ownerName", containsInAnyOrder("Member A", "Member B")));

        mockMvc.perform(get("/reports")
                        .header("Authorization", bearer(manager))
                        .param("userId", memberA.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].ownerName").value("Member A"));
    }

    @Test
    void anAdminIsForbiddenFromBothListingAndViewingReportDetail() throws Exception {
        User admin = persistUser("Admin", "admin@example.com", Role.ADMIN);
        User member = persistUser("Member", "member@example.com", Role.MEMBER);
        Project project = persistProject();
        long reportId = createReport(member, project.getId(), "2026-09-01", "2026-09-05");

        mockMvc.perform(get("/reports").header("Authorization", bearer(admin)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/reports/" + reportId).header("Authorization", bearer(admin)))
                .andExpect(status().isForbidden());
    }

    @Test
    void aMemberIsDeniedAnotherMembersReportDetailButAManagerCanViewAny() throws Exception {
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        User owner = persistUser("Owner", "owner@example.com", Role.MEMBER);
        User otherMember = persistUser("Other Member", "other@example.com", Role.MEMBER);
        Project project = persistProject();
        long reportId = createReport(owner, project.getId(), "2026-09-01", "2026-09-05");

        mockMvc.perform(get("/reports/" + reportId).header("Authorization", bearer(otherMember)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/reports/" + reportId).header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName").value("Owner"));

        mockMvc.perform(get("/reports/" + reportId).header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName").value("Owner"))
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void paginationActuallyWorksAcrossTwoPages() throws Exception {
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        User member = persistUser("Member", "member@example.com", Role.MEMBER);
        Project project = persistProject();
        createReport(member, project.getId(), "2026-09-01", "2026-09-05");
        createReport(member, project.getId(), "2026-09-08", "2026-09-12");
        createReport(member, project.getId(), "2026-09-15", "2026-09-19");

        mockMvc.perform(get("/reports")
                        .header("Authorization", bearer(manager))
                        .param("size", "2")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0));

        mockMvc.perform(get("/reports")
                        .header("Authorization", bearer(manager))
                        .param("size", "2")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.number").value(1));
    }

    private long createReport(User owner, Long projectId, String weekStart, String weekEnd) throws Exception {
        String body = """
                {"projectId": %d, "weekStart": "%s", "weekEnd": "%s"}
                """
                .formatted(projectId, weekStart, weekEnd);

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
