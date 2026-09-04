package com.weeklyreport.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Project;
import com.weeklyreport.backend.domain.ProjectAssignment;
import com.weeklyreport.backend.domain.ProjectAssignmentId;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clearTables() {
        projectAssignmentRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void savesAndReloadsAProject() {
        Project project = new Project();
        project.setName("Weekly Report Generator");
        project.setDescription("Internal tool for weekly status reports");
        project.setActive(false);

        Long savedId = projectRepository.save(project).getId();

        Project reloaded = projectRepository.findById(savedId).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Weekly Report Generator");
        assertThat(reloaded.getDescription()).isEqualTo("Internal tool for weekly status reports");
        assertThat(reloaded.isActive()).isFalse();
    }

    @Test
    void defaultsActiveToTrueAndAllowsDescriptionToBeNull() {
        Project project = new Project();
        project.setName("No description yet");

        Long savedId = projectRepository.save(project).getId();

        Project reloaded = projectRepository.findById(savedId).orElseThrow();
        assertThat(reloaded.isActive()).isTrue();
        assertThat(reloaded.getDescription()).isNull();
    }

    @Test
    void findByActiveTrueReturnsOnlyActiveProjects() {
        Project active = new Project();
        active.setName("Active project");
        active.setActive(true);
        projectRepository.save(active);

        Project inactive = new Project();
        inactive.setName("Archived project");
        inactive.setActive(false);
        projectRepository.save(inactive);

        assertThat(projectRepository.findByActiveTrue())
                .extracting(Project::getName)
                .containsExactly("Active project");
    }

    @Test
    void savesAndReloadsAProjectAssignmentByItsCompositeKey() {
        User user = new User();
        user.setName("Ada Lovelace");
        user.setEmail("ada@example.com");
        user.setPasswordHash("not-a-real-hash");
        user.setRole(Role.MEMBER);
        user.setStatus(UserStatus.ACTIVE);
        Long userId = userRepository.save(user).getId();

        Project project = new Project();
        project.setName("Weekly Report Generator");
        Long projectId = projectRepository.save(project).getId();

        ProjectAssignmentId assignmentId = new ProjectAssignmentId(userId, projectId);
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setId(assignmentId);
        projectAssignmentRepository.save(assignment);

        ProjectAssignment reloaded = projectAssignmentRepository.findById(assignmentId).orElseThrow();
        assertThat(reloaded.getId().userId()).isEqualTo(userId);
        assertThat(reloaded.getId().projectId()).isEqualTo(projectId);
    }
}
