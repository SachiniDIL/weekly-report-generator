package com.weeklyreport.backend.controller;

import com.weeklyreport.backend.dto.ProjectMemberView;
import com.weeklyreport.backend.dto.ProjectRequest;
import com.weeklyreport.backend.dto.ProjectResponse;
import com.weeklyreport.backend.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@PreAuthorize("hasRole('MANAGER')")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> listProjects(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return projectService.listProjects(includeInactive);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody ProjectRequest request) {
        return projectService.createProject(request);
    }

    @PutMapping("/{id}")
    public ProjectResponse updateProject(
            @PathVariable long id, @Valid @RequestBody ProjectRequest request) {
        return projectService.updateProject(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveProject(@PathVariable long id) {
        projectService.archiveProject(id);
    }

    @GetMapping("/{id}/members")
    public List<ProjectMemberView> listMembers(@PathVariable long id) {
        return projectService.listMembers(id);
    }

    @PostMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMemberView assignMember(@PathVariable long id, @PathVariable long userId) {
        return projectService.assignMember(id, userId);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassignMember(@PathVariable long id, @PathVariable long userId) {
        projectService.unassignMember(id, userId);
    }
}
