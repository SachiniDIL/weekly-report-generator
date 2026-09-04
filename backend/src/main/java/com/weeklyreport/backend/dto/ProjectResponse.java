package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.Project;

public record ProjectResponse(Long id, String name, String description, boolean active) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(), project.getName(), project.getDescription(), project.isActive());
    }
}
