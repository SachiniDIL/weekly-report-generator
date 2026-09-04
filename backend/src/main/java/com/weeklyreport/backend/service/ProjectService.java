package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.Project;
import com.weeklyreport.backend.domain.ProjectAssignment;
import com.weeklyreport.backend.domain.ProjectAssignmentId;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.dto.ProjectMemberView;
import com.weeklyreport.backend.dto.ProjectRequest;
import com.weeklyreport.backend.dto.ProjectResponse;
import com.weeklyreport.backend.exception.AdminNotAssignableException;
import com.weeklyreport.backend.exception.ProjectAlreadyArchivedException;
import com.weeklyreport.backend.exception.ProjectAssignmentNotFoundException;
import com.weeklyreport.backend.exception.ProjectMemberAlreadyAssignedException;
import com.weeklyreport.backend.exception.ProjectNotFoundException;
import com.weeklyreport.backend.exception.UserNotFoundException;
import com.weeklyreport.backend.repository.ProjectAssignmentRepository;
import com.weeklyreport.backend.repository.ProjectRepository;
import com.weeklyreport.backend.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manager-only project lifecycle and team-assignment logic. */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(boolean includeInactive) {
        List<Project> projects =
                includeInactive ? projectRepository.findAll() : projectRepository.findByActiveTrue();
        return projects.stream().map(ProjectResponse::from).toList();
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse updateProject(long id, ProjectRequest request) {
        Project project = getProject(id);
        project.setName(request.name());
        project.setDescription(request.description());
        return ProjectResponse.from(project);
    }

    /** "Delete" a project means archiving it — reports already reference it by FK. */
    @Transactional
    public void archiveProject(long id) {
        Project project = getProject(id);
        if (!project.isActive()) {
            throw new ProjectAlreadyArchivedException(id);
        }
        project.setActive(false);
    }

    @Transactional
    public ProjectMemberView assignMember(long projectId, long userId) {
        getProject(projectId);
        User user = getUser(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new AdminNotAssignableException();
        }

        ProjectAssignmentId assignmentId = new ProjectAssignmentId(userId, projectId);
        if (projectAssignmentRepository.existsById(assignmentId)) {
            throw new ProjectMemberAlreadyAssignedException(projectId, userId);
        }

        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setId(assignmentId);
        projectAssignmentRepository.save(assignment);
        return ProjectMemberView.from(user);
    }

    @Transactional
    public void unassignMember(long projectId, long userId) {
        ProjectAssignmentId assignmentId = new ProjectAssignmentId(userId, projectId);
        if (!projectAssignmentRepository.existsById(assignmentId)) {
            throw new ProjectAssignmentNotFoundException(projectId, userId);
        }
        projectAssignmentRepository.deleteById(assignmentId);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberView> listMembers(long projectId) {
        getProject(projectId);
        List<Long> memberIds = projectAssignmentRepository.findById_ProjectId(projectId).stream()
                .map(assignment -> assignment.getId().userId())
                .toList();
        return userRepository.findAllById(memberIds).stream().map(ProjectMemberView::from).toList();
    }

    private Project getProject(long id) {
        return projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
    }

    private User getUser(long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}
