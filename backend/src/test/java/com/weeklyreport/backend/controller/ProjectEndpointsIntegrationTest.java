package com.weeklyreport.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Project;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.repository.ProjectAssignmentRepository;
import com.weeklyreport.backend.repository.ProjectRepository;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.security.JwtService;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProjectEndpointsIntegrationTest {

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

    @BeforeEach
    void clearTables() {
        projectAssignmentRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    static Stream<Arguments> nonManagerRoleAndEndpoint() {
        return List.of(Role.MEMBER, Role.ADMIN).stream()
                .flatMap(role -> projectEndpoints().map(endpoint -> Arguments.of(role, endpoint)));
    }

    private static Stream<Named<MockHttpServletRequestBuilder>> projectEndpoints() {
        return Stream.of(
                Named.of("GET /projects", get("/projects")),
                Named.of(
                        "POST /projects",
                        post("/projects").contentType(MediaType.APPLICATION_JSON).content(projectBody("X"))),
                Named.of(
                        "PUT /projects/1",
                        put("/projects/1").contentType(MediaType.APPLICATION_JSON).content(projectBody("X"))),
                Named.of("DELETE /projects/1", delete("/projects/1")),
                Named.of("GET /projects/1/members", get("/projects/1/members")),
                Named.of("POST /projects/1/members/1", post("/projects/1/members/1")),
                Named.of("DELETE /projects/1/members/1", delete("/projects/1/members/1")));
    }

    @ParameterizedTest(name = "{0} is forbidden from {1}")
    @MethodSource("nonManagerRoleAndEndpoint")
    void nonManagerRolesAreForbiddenFromEveryProjectEndpoint(
            Role role, MockHttpServletRequestBuilder request) throws Exception {
        User user = persistUser("Non Manager", "nonmanager@example.com", role);

        mockMvc.perform(request.header("Authorization", bearer(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void creatingAndArchivingAProjectWorks() throws Exception {
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);

        String responseBody = mockMvc.perform(post("/projects")
                        .header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody("Weekly Report Generator")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Weekly Report Generator"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long projectId = ((Number) JsonPath.read(responseBody, "$.id")).longValue();

        mockMvc.perform(delete("/projects/" + projectId).header("Authorization", bearer(manager)))
                .andExpect(status().isNoContent());
        assertThat(projectRepository.findById(projectId).orElseThrow().isActive()).isFalse();

        mockMvc.perform(delete("/projects/" + projectId).header("Authorization", bearer(manager)))
                .andExpect(status().isConflict());
    }

    @Test
    void assigningAMemberWorksAndRejectsADuplicateAssignment() throws Exception {
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        User member = persistUser("Member", "member@example.com", Role.MEMBER);
        Project project = persistProject("Weekly Report Generator");

        mockMvc.perform(post("/projects/" + project.getId() + "/members/" + member.getId())
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(member.getId()))
                .andExpect(jsonPath("$.name").value("Member"))
                .andExpect(jsonPath("$.email").value("member@example.com"));

        mockMvc.perform(post("/projects/" + project.getId() + "/members/" + member.getId())
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isConflict());
    }

    @Test
    void assigningAnAdminIsRejected() throws Exception {
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        User admin = persistUser("Admin", "admin@example.com", Role.ADMIN);
        Project project = persistProject("Weekly Report Generator");

        mockMvc.perform(post("/projects/" + project.getId() + "/members/" + admin.getId())
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isConflict());
    }

    @Test
    void unassigningWorksAndRejectsANonExistentAssignment() throws Exception {
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        User member = persistUser("Member", "member@example.com", Role.MEMBER);
        Project project = persistProject("Weekly Report Generator");

        mockMvc.perform(post("/projects/" + project.getId() + "/members/" + member.getId())
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/projects/" + project.getId() + "/members/" + member.getId())
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/projects/" + project.getId() + "/members/" + member.getId())
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listMembersReturnsTheCorrectSetAfterAssignAndUnassign() throws Exception {
        User manager = persistUser("Manager", "manager@example.com", Role.MANAGER);
        User memberA = persistUser("Member A", "membera@example.com", Role.MEMBER);
        User memberB = persistUser("Member B", "memberb@example.com", Role.MEMBER);
        Project project = persistProject("Weekly Report Generator");

        mockMvc.perform(post("/projects/" + project.getId() + "/members/" + memberA.getId())
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/projects/" + project.getId() + "/members/" + memberB.getId())
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/projects/" + project.getId() + "/members")
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(delete("/projects/" + project.getId() + "/members/" + memberA.getId())
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/projects/" + project.getId() + "/members")
                        .header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(memberB.getId()));
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

    private Project persistProject(String name) {
        Project project = new Project();
        project.setName(name);
        return projectRepository.save(project);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private static String projectBody(String name) {
        return """
                {"name": "%s", "description": "A test project"}
                """.formatted(name);
    }
}
