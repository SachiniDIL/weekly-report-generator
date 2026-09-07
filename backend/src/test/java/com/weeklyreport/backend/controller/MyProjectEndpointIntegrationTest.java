package com.weeklyreport.backend.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Project;
import com.weeklyreport.backend.domain.ProjectAssignment;
import com.weeklyreport.backend.domain.ProjectAssignmentId;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.repository.ProjectAssignmentRepository;
import com.weeklyreport.backend.repository.ProjectRepository;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.security.JwtService;
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
class MyProjectEndpointIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User member;

    @BeforeEach
    void setUp() {
        clearTables();
        member = persistUser("Mia Member", "mia@example.com", Role.MEMBER);
    }

    @AfterEach
    void tearDown() {
        clearTables();
    }

    @Test
    void returnsOnlyTheMembersOwnActiveAssignedProjects() throws Exception {
        Project apollo = persistProject("Apollo", true);
        Project zephyr = persistProject("Zephyr", true);
        Project archived = persistProject("Archived", false);
        Project unassigned = persistProject("Unassigned", true);

        assign(member, apollo);
        assign(member, zephyr);
        assign(member, archived);

        User otherMember = persistUser("Other", "other@example.com", Role.MEMBER);
        assign(otherMember, unassigned);

        mockMvc.perform(get("/me/projects").header("Authorization", bearer(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Apollo", "Zephyr")));
    }

    @Test
    void returnsAnEmptyListWhenTheMemberHasNoAssignments() throws Exception {
        persistProject("Apollo", true);

        mockMvc.perform(get("/me/projects").header("Authorization", bearer(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void aManagerCanAlsoReadTheirOwnAssignments() throws Exception {
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        Project apollo = persistProject("Apollo", true);
        assign(manager, apollo);

        mockMvc.perform(get("/me/projects").header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Apollo"));
    }

    @Test
    void rejectsAnUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/me/projects")).andExpect(status().is4xxClientError());
    }

    private void assign(User user, Project project) {
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setId(new ProjectAssignmentId(user.getId(), project.getId()));
        projectAssignmentRepository.save(assignment);
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

    private Project persistProject(String name, boolean active) {
        Project project = new Project();
        project.setName(name);
        project.setActive(active);
        return projectRepository.save(project);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private void clearTables() {
        projectAssignmentRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }
}
