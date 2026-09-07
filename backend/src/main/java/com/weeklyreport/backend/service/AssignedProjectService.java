package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.Project;
import com.weeklyreport.backend.dto.ProjectResponse;
import com.weeklyreport.backend.repository.ProjectAssignmentRepository;
import com.weeklyreport.backend.repository.ProjectRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * One member's own project list — the active projects they're assigned to, for the
 * create-report picker. Kept apart from {@link ProjectService} (manager-only lifecycle and
 * team assignment) because this is member-facing and strictly read-only.
 */
@Service
public class AssignedProjectService {

    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final ProjectRepository projectRepository;

    public AssignedProjectService(
            ProjectAssignmentRepository projectAssignmentRepository, ProjectRepository projectRepository) {
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listActiveProjectsFor(long userId) {
        List<Long> assignedProjectIds = projectAssignmentRepository.findById_UserId(userId).stream()
                .map(assignment -> assignment.getId().projectId())
                .toList();

        return projectRepository.findAllById(assignedProjectIds).stream()
                .filter(Project::isActive)
                .map(ProjectResponse::from)
                .toList();
    }
}
