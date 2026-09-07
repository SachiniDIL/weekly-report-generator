package com.weeklyreport.backend.controller;

import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.dto.ProjectResponse;
import com.weeklyreport.backend.service.AssignedProjectService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The authenticated user's own project assignments. No role gate — it only ever returns the
 * caller's own data — so a MEMBER can use it for the create-report project picker without
 * touching the manager-only {@code /projects} endpoints.
 */
@RestController
@RequestMapping("/me/projects")
public class MyProjectController {

    private final AssignedProjectService assignedProjectService;

    public MyProjectController(AssignedProjectService assignedProjectService) {
        this.assignedProjectService = assignedProjectService;
    }

    @GetMapping
    public List<ProjectResponse> listMyProjects(@AuthenticationPrincipal User user) {
        return assignedProjectService.listActiveProjectsFor(user.getId());
    }
}
